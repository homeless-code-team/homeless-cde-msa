package com.spring.homelesscode.friends_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.spring.homelesscode.friends_service.config")
@SpringBootApplication
public class FriendsServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FriendsServerApplication.class, args);
    }

}
