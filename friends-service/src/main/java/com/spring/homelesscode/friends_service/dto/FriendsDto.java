package com.spring.homelesscode.friends_service.dto;


import com.spring.homelesscode.friends_service.entity.AddStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendsDto {
    private String receiverNickname;
    private AddStatus addStatus;
}
