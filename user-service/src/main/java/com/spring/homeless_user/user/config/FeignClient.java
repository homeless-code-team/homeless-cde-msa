package com.spring.homeless_user.user.config;

import com.spring.homeless_user.user.dto.FeignDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@org.springframework.cloud.openfeign.FeignClient(name = "sever-service")
public interface FeignClient {
    @PostMapping("/users/server")
    FeignDto getUserIdByServerId(@RequestBody FeignDto dto);
}
