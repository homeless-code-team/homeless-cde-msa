package com.spring.homelesscode.friends_service.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class FriendtoRepositoryDto {

    private String senderEmail;
    private String receiverEmail;
}
