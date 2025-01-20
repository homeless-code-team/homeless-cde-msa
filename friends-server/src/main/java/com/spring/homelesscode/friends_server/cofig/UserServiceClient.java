package com.spring.homelesscode.friends_server.cofig;

import com.spring.homelesscode.friends_server.dto.FeignResDto;
import com.spring.homelesscode.friends_server.dto.FriendsDto;
import com.spring.homelesscode.friends_server.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserServiceClient {

    //이메일 전달 닉네임 상태 받음
    @PostMapping("/api/v1/users/details-by-email")
   List<FeignResDto> getUserDetails(@RequestBody List<String> result);

    // 이메일 전송 닉네임 전달
    @GetMapping("/api/v1/users/getemail")
    String getEmail(@RequestParam("nickname") String nickname);
}

