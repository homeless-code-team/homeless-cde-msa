package com.homeless.chatservice.entity;

import com.homeless.chatservice.dto.MessageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chatMessages")  // MongoDB 컬렉션 지정
@CompoundIndex(def = "{'serverId', 'channelId': 1}")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id  // MongoDB에서 id는 _id로 자동 설정됨
    private String id;  // MongoDB에서는 String 타입을 사용하는 경우가 많음
    @NotNull
    private String email;
    @NotNull
    private String serverId;
    @NotNull
    private String channelId;
    @NotNull
    private String writer;
    @NotNull(message = "Message content cannot be null")
    @Size(min = 1, max = 1000, message = "Message content must be between 1 and 1000 characters")
    private String content;
    @NotNull
    private MessageType messageType;
    @CreatedDate
    private Long timestamp;

    private String fileUrl;
    private String fileName;

    public void setContent(String reqMessage) {

    }
}