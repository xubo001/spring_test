package org.firewater.service.user;

import org.firewater.spring.annotation.Component;

@Component
public class UserService {

    public void aaa(){
        System.out.println("hahahaha");
    }
    public String haha1(){
        return "I'm haha1";
    }

    public Integer haha2(){
        return new Integer(100);
    }
}
