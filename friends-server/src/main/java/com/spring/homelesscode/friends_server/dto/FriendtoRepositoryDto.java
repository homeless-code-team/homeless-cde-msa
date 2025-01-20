package com.spring.homelesscode.friends_server.dto;

import jakarta.persistence.Column;
import lombok.*;

@Getter@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class FriendtoRepositoryDto {

    private String senderEmail;
    private String receiverEmail;
}
