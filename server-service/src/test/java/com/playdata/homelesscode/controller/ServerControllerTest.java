package com.playdata.homelesscode.controller;

import com.playdata.homelesscode.entity.Server;
import com.playdata.homelesscode.repository.ServerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest
class ServerControllerTest {

    @Autowired
    private ServerRepository serverRepository;


    @Test
    public void createServersTest() {
        for (int i = 1; i <= 100; i++) {
            Server server = Server.builder()
                    .email("maru@naver.com")
                    .title("Server Title " + i)
                    .tag("Tag " + i)
                    .serverImg("Image URL " + i)
                    .serverType(i % 3) // 0, 1, 2 반복
                    .build();

            serverRepository.save(server);
        }

        System.out.println("100 servers created successfully!");
    }

}