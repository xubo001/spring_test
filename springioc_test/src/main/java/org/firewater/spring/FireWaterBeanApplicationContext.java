package org.firewater.spring;

import org.firewater.spring.annotation.*;
import org.firewater.spring.beans.BeanDefinition;
import org.firewater.spring.beans.BeanNameAware;
import org.firewater.spring.beans.InitializingBean;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FireWaterBeanApplicationContext {
    private Class<?> configClass;

    private Map<String, BeanDefinition> definitionMap=new ConcurrentHashMap<String, BeanDefinition>();
    private Map<String, Object> singletonObject=new ConcurrentHashMap<String, Object>();

    public FireWaterBeanApplicationContext(Class<?> configClass) {
        this.configClass = configClass;
        //获取配置类中扫描的路径
        String packageUrl=scanConfigClass(configClass);
        //扫描类，初始化BeanDefinition
        scanUrl(packageUrl);
        //初始化非懒加载单例类
        initNonLazySingletonClassInstance();

    }

    private void initNonLazySingletonClassInstance() {
        for (String beanName:
        definitionMap.keySet() ) {
            BeanDefinition bd=definitionMap.get(beanName);
            Class clazz =bd.getClazz();
            if(clazz.isAnnotationPresent(Component.class)) {
                if (!bd.isLazy()) {
                    if ("singleton".equals(bd.getScope())) {
                        Object o = createBean(beanName, bd);
                        singletonObject.put(beanName, o);
                    }
                }
            }
        }

    }

    private Object createBean(String beanName, BeanDefinition bd) {
        Object o=null;
        if(singletonObject.containsKey(beanName))return singletonObject.get(beanName);
        try {
            o = bd.getClazz().newInstance();
            if("singleton".equals(bd.getScope())){
                singletonObject.put(beanName,o);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return o;
    }

    private void scanUrl(String packageUrl) {
        String url=packageUrl.replaceAll("\\.","/");
        URL resource = this.getClass().getClassLoader().getResource(url);
        File file= new File(resource.getFile());
        for (File file1:
             file.listFiles()) {
            if(file1.isDirectory()){
                scanUrl(packageUrl+"."+file1.getName());
            }else{
                try {
                    String className=file1.getName().replaceAll("\\.class","");
                    Class<?> clazz = Class.forName(packageUrl + "." + className);
                    String beanName=toFirstLower(className);
                    Lazy annotation = clazz.getAnnotation(Lazy.class);
                    boolean isLazy =(annotation==null)?false :annotation.value();
                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                    String scope =( scopeAnnotation==null)?"singleton":scopeAnnotation.value();
                    BeanDefinition beanDefinition=new BeanDefinition(beanName,isLazy,scope,clazz);
                    definitionMap.put(beanName,beanDefinition);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private String toFirstLower(String simpleName) {
        return simpleName.substring(0,1).toLowerCase()+simpleName.substring(1);
    }

    private String scanConfigClass(Class<?> configClass) {
        String url="";
        if(configClass.isAnnotationPresent(ComponentScan.class)){
            url = configClass.getAnnotation(ComponentScan.class).value();
        }
        return url;
    }

    public Object getBean(String beanName){
        if(!definitionMap.containsKey(beanName)) return null;
        BeanDefinition bd = definitionMap.get(beanName);
        Class clazz=bd.getClazz();
        Object bean = createBean(beanName, bd);
        for (Field field:
        clazz.getDeclaredFields() ) {
            if(field.isAnnotationPresent(Autowired.class)){
                field.setAccessible(true);
                try {
                    field.set(bean,getBean(field.getName()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if(bean instanceof BeanNameAware ) {
            ((BeanNameAware) bean).setBeanName(beanName);
        }
        if(bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }


        return bean;
    }



//    if(clazz.isAnnotationPresent(Component.class)) {
//        if(clazz.isAnnotationPresent(Lazy.class)){
//            boolean isLazy = clazz.getAnnotation(Lazy.class).value();
//            if(!isLazy){
//                Object o = clazz.newInstance();
//
//            }
//        }else{
//            if(clazz.isAnnotationPresent(Scope.class)){
//                Object o = clazz.newInstance();
//            }
//
//        }
//
//    }
}
