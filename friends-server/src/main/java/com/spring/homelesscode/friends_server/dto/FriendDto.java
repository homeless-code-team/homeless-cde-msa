package com.spring.homelesscode.friends_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendFetchDto {
    private String id;          // Friends 객체의 ID
    private String email;       // 친구 이메일
    private String status;      // 상태 정보
}
