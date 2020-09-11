package org.firewater.user.service.impl;

import org.firewater.spring.mvc.service.UserService;
import org.firewater.framework.annotation.FWService;

@FWService
public class UserServiceImpl implements UserService {
    public String test(){
        return "mvc source";
    }

    @Override
    public String test1(String name) {
        return name+": hello ";
    }
}
