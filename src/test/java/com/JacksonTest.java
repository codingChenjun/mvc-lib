package com;

import com.nf.mvc.util.JacksonUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class JacksonTest {
    @Test
    public void test1(){
        User user = new User();
        user.setId(3l);
        user.setFirstName("chen");
        user.setLastName("jun");

        String json = JacksonUtils.toJson(user);
        System.out.println(json);
    }

    @Test
    public void test2(){
        User user = new User();
        user.setId(3l);
        user.setFirstName("chen");
        user.setLastName("jun");

        Response<User> response = new Response<>();
        response.setResult(user);
        String json = JacksonUtils.toJson(response);
        System.out.println(json);
        System.out.println("====================");
        Response<User> json2 = JacksonUtils.fromJson(json, Response.class, User.class);
        System.out.println(json2);
    }


    @Test
    public void test3(){
        User user = new User();
        user.setId(3l);
        user.setFirstName("chen");
        user.setLastName("jun");

        User user2 = new User();
        user2.setId(32l);
        user2.setFirstName("chen2");
        user2.setLastName("jun2");
        List<User> list = new ArrayList<>();
        list.add(user);
        list.add(user2);
        String json = JacksonUtils.toJson(list);
        System.out.println(json);
        System.out.println("====================");
        List<User> json2 = JacksonUtils.fromJson(json, List.class, User.class);
        System.out.println(json2);

    }
}
