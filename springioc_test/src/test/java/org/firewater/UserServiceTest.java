package org.firewater;


import org.firewater.service.order.OrderService;
import org.firewater.spring.FireWaterBeanApplicationContext;
import org.firewater.spring.config.AppConfig;

public class UserServiceTest {

    public static void main(String[] args) {
        FireWaterBeanApplicationContext fireWaterBeanApplicationContext=new FireWaterBeanApplicationContext(AppConfig.class);
        OrderService orderService = (OrderService)fireWaterBeanApplicationContext.getBean("orderService");
        OrderService orderService1 = (OrderService)fireWaterBeanApplicationContext.getBean("orderService");
        OrderService orderService2 = (OrderService)fireWaterBeanApplicationContext.getBean("orderService");
        OrderService orderService3 = (OrderService)fireWaterBeanApplicationContext.getBean("orderService");
        System.out.println(orderService);
        System.out.println(orderService1);
        System.out.println(orderService2);
        System.out.println(orderService3);
        orderService.test();
    }
}
