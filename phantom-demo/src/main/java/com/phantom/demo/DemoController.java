package com.phantom.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by pphh on 2022/8/4.
 */
@RestController
@RequestMapping("/api")
public class DemoController {

    public List<String> l = new ArrayList<>();
    
    @GetMapping("/hello")
    public String sayHello() {
        
        l.add("111");
        l.add("222");
        l.add("333");
        System.out.println("old before");
        sayOOO();
        System.out.println("old end");

        
        return "hello,world";
    }

    public void sayOOO() {
        l.add("000000");
        l.add("999999");
        l.add("888888");
    }

}
