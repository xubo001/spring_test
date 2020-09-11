package org.firewater.spring.mvc.controller;

import org.firewater.spring.mvc.service.UserService;
import org.firewater.springmvc.dispathcer.annotation.FWAutoWired;
import org.firewater.springmvc.dispathcer.annotation.FWController;
import org.firewater.springmvc.dispathcer.annotation.FWRequestMapping;
import org.firewater.springmvc.dispathcer.annotation.FWRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@FWController
@FWRequestMapping("/user")
public class UserController {
    @FWAutoWired
    private UserService userService;
    @FWRequestMapping("/test")
    public String test(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        System.out.println("test");
        String test="";
        try {
            test = userService.test();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            httpServletResponse.getWriter().write(test);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return test;
    }


    @FWRequestMapping("/test1")
    public String test1(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,@FWRequestParam("name") String name){
        System.out.println("test1");
        String test="";
        try {
            test = userService.test1(name);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            httpServletResponse.getWriter().write(test);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return test;
    }


    @FWRequestMapping("/test2")
    public String test2(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                        @FWRequestParam("name") String name,@FWRequestParam("haha") Integer haha){
        System.out.println("test2");
        String test="";
        try {
            test = userService.test1(name);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            httpServletResponse.getWriter().write(test+haha);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return test;
    }


}
