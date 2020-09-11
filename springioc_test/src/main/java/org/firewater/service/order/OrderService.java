package org.firewater.service.order;

import org.firewater.service.user.UserService;
import org.firewater.spring.annotation.Autowired;
import org.firewater.spring.annotation.Component;
import org.firewater.spring.annotation.Lazy;
import org.firewater.spring.annotation.Scope;
import org.firewater.spring.beans.BeanNameAware;
import org.firewater.spring.beans.InitializingBean;

@Component
@Lazy
@Scope("singleton")
public class OrderService implements BeanNameAware, InitializingBean {
    @Autowired
    private UserService userService;

    private String beanName;

    private String haha;

    private Integer haha2;

    public void test(){
        userService.aaa();
        System.out.println("this.beanName==="+beanName);
        System.out.println("this.haha==="+haha);
        System.out.println("this.haha2==="+haha2);
    }

    @Override
    public void setBeanName(String name) {
            this.beanName=name;
    }

    @Override
    public void afterPropertiesSet()  {
            this.haha=userService.haha1();
            this.haha2=userService.haha2();
    }
}
