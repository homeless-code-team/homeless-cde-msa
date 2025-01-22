package com.playdata.homelesscode.client;

import com.playdata.homelesscode.dto.user.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@FeignClient(name = "user-service")
public interface UserServiceClient {

    @PostMapping("api/v1/users/userList")
    List<UserResponseDto> findByEmailIn(@RequestBody List<String> userEmails);
}
