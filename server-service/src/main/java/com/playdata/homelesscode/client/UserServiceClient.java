package com.playdata.homelesscode.client;

import com.playdata.homelesscode.common.dto.CommonResDto;
import com.playdata.homelesscode.dto.user.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient( name = "user-service")
public interface UserServiceClient {

    @GetMapping("/memberId")
    CommonResDto<UserResponseDto> findById(@PathVariable String id);



}
