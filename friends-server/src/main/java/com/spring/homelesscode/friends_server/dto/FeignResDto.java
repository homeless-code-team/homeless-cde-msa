package com.spring.homelesscode.friends_server.dto;

import lombok.*;

@Getter@Setter@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FeignResDto {

    private String receiverNickname;
    private String profileImage;
}
