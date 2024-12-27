package com.homelesscode.configserivce;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;


@SpringBootApplication
@EnableConfigServer
public class ConfigSerivceApplication {


    public static void main(String[] args) {
        SpringApplication.run(ConfigSerivceApplication.class, args);
    }

}
