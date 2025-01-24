package com.spring.homelesscode.friends_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.spring.homelesscode.friends_server.config")
@SpringBootApplication
public class FriendsServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FriendsServerApplication.class, args);
    }

}
