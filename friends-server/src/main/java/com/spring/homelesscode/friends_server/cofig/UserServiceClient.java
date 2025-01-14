package com.spring.homelesscode.friends_server.cofig;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/exists-refresh-token")
    Boolean existsByNicknameAndRefreshToken(@RequestParam("nickname") String nickname);
}

