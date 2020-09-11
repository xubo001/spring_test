package org.firewater.springmvc.dispathcer;


import org.firewater.springmvc.dispathcer.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class FireWaterDispatcherV2 extends HttpServlet {
    //环境属性
    private Properties properties=new Properties();;
    //扫描出来的类名字列表
    private List<String> classList=new ArrayList<String>();
    //Spring类的容器 这里有很多简略的步骤，比如lazy判断、单例判断、bean的定义等等都省略了
    Map<String,Object> ioc=new HashMap<String,Object>();
    //handleMapping
    List<Handler> handleMapping=new ArrayList<Handler>();
    //
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispathcer(req,resp);
    }

    private void doDispathcer(HttpServletRequest req, HttpServletResponse resp) {
        String url=req.getRequestURI();
        if("/favicon.ico".equals(url)){
            return ;
        }
        for (Handler handler:
        handleMapping) {
            if(handler.getUrl().equals(url)){
                Method method=handler.getMethod();
                Class<?>[] parameterTypes = method.getParameterTypes();
                Object[] objects=new Object[method.getParameterCount()];
                Map<String,Integer> map=handler.getMap();
                if(map.containsKey("HttpServletRequest")){
                    Integer index=map.get("HttpServletRequest");
                    objects[index]=req;
                }
                if(map.containsKey("HttpServletResponse")){
                    Integer index=map.get("HttpServletResponse");
                    objects[index]=resp;
                }
                Map<String, String[]> parameterMap = req.getParameterMap();
                for (String key:
                parameterMap.keySet()) {
                    if(map.containsKey(key)){
                        Integer index = map.get(key);
                        objects[index]=convert(parameterTypes[index],Arrays.toString(parameterMap.get(key))
                                .replaceAll("\\[|\\]","")
                                .replaceAll("\\s",",").replaceAll(",+",","));
                    }
                }
                try {
                    method.invoke(handler.getObject(),objects);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    //此处只是为了mvc的思想，没有做成策略模式，Spring中有各种的对象类型convert
    public Object convert(Class clazz,String value){
        if(clazz==Integer.class){
            return new Integer(value);
        }
        return value;
    }
    @Override
    public void init(ServletConfig config) throws ServletException {
        System.out.println("************************************init begin************************************");
        //1、加载配置文件
        loadProperties(config);

        //2、扫描类
        scanner(properties.getProperty("scan_class"));
        
        //3、实例化类
        instanceClass();

        //4、依赖注入
        di();

        //5、handleMapping
        initHandlerMapping();
        System.out.println("************************************init end************************************");
    }

    private void initHandlerMapping() {
        for (String beanName:ioc.keySet()
             ) {
            Object o = ioc.get(beanName);
            Class<?> clazz=o.getClass();
            if(clazz.isAnnotationPresent(FWController.class)){
                String baseURL = clazz.getAnnotation(FWRequestMapping.class).value();
                Method[] declaredMethods = clazz.getDeclaredMethods();
                for (int i = 0; i < declaredMethods.length; i++) {
                    if(declaredMethods[i].isAnnotationPresent(FWRequestMapping.class)){
                        String methoodURL = declaredMethods[i].getAnnotation(FWRequestMapping.class).value();
                        Handler handler=new Handler(baseURL+methoodURL,declaredMethods[i],o);
                        handleMapping.add(handler);
                    }
                    
                }
            }
        }

    }

    private void di() {
        for (String beanName:ioc.keySet()) {
            try {
                Object o = ioc.get(beanName);
                Field[] declaredFields = o.getClass().getDeclaredFields();
                for (Field field:
                        declaredFields) {
                    if(field.isAnnotationPresent(FWAutoWired.class)){
                        FWAutoWired annotation = field.getAnnotation(FWAutoWired.class);
                        String autoWiredValue=annotation.value();
                        String fieldBeanName=(autoWiredValue==null||"".endsWith(autoWiredValue))?toFirstLower(field.getType().getSimpleName()):autoWiredValue;
                        field.setAccessible(true);
                        field.set(o,ioc.get(fieldBeanName));
                    }
                }
            }  catch (IllegalAccessException e) {
                e.printStackTrace();
            }


        }
    }

    private void instanceClass() {
        for (String className:classList) {
            Class<?> aClass = null;
            try {
                aClass = Class.forName(className);
                if(aClass.isAnnotationPresent(FWController.class)){
                    Constructor<?> declaredConstructor = aClass.getDeclaredConstructor();
                    Object o = declaredConstructor.newInstance();
                    String beanName = aClass.getAnnotation(FWController.class).value();
                    beanName=(beanName==null||"".endsWith(beanName))?toFirstLower(aClass.getSimpleName()):beanName;
                    ioc.put(beanName,o);
                }else if(aClass.isAnnotationPresent(FWService.class)){
                    Constructor<?> declaredConstructor = aClass.getDeclaredConstructor();
                    Object o = declaredConstructor.newInstance();
                    String beanName;
                    beanName = aClass.getAnnotation(FWService.class).value();
                    beanName = (beanName == null || "".endsWith(beanName)) ? toFirstLower(aClass.getSimpleName()) : beanName;
                    ioc.put(beanName, o);
                    if(aClass.getInterfaces()!=null) {
                        for (Class clazz :
                                aClass.getInterfaces()) {
                            if (!ioc.containsKey(toFirstLower(clazz.getName()))) {
                                ioc.put(toFirstLower(clazz.getSimpleName()), o);
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }

    private String toFirstLower(String simpleName) {
        return simpleName.substring(0,1).toLowerCase()+simpleName.substring(1);
    }


    private void scanner(String scan_class) {

        URL resource = this.getClass().getClassLoader().getResource("/"+scan_class.replaceAll("\\.", "/"));
        File files=new File(resource.getFile());
        for (File file :files.listFiles()){
            if(file.isDirectory()){
                scanner(scan_class+"."+file.getName());
            }
            else {
                if(file.getName().endsWith(".class")) {
                    this.classList.add(scan_class + "."+file.getName().replaceAll("\\.class$", ""));
                }
            }
        }
    }

    private void loadProperties(ServletConfig config) {
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(resourceAsStream!=null){
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public class Handler{
        private String url;
        private Method method;
        private Object object;
        //定义方法中参数的类型和位置
        private Map<String, Integer> map=new HashMap<String, Integer>();

        public Handler(String url, Method method, Object object) {
            this.url = url;
            this.method = method;
            this.object = object;
            this.initHandlerPos(method);
        }
        public void initHandlerPos(Method method){
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation annotation:
                parameterAnnotations[i]) {
                    if(annotation instanceof  FWRequestParam){
                        String value = ((FWRequestParam) annotation).value();
                        if(value!=null&&!"".equals(value)){
                            map.put(value,i);
                        }
                    }
                }
                
            }
            for (int i = 0; i < parameterTypes.length; i++) {
                if(parameterTypes[i]== HttpServletRequest.class){
                    map.put(HttpServletRequest.class.getSimpleName(),i);
                }else  if(parameterTypes[i]== HttpServletResponse.class){
                    map.put(HttpServletResponse.class.getSimpleName(),i);
                }
            }
        }

        public String getUrl() {
            return url;
        }

        public Method getMethod() {
            return method;
        }

        public Object getObject() {
            return object;
        }

        public Map<String, Integer> getMap() {
            return map;
        }
    }
}
