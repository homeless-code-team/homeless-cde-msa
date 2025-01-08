package com.homeless.chatservice.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    private Long serverId;
    private Long channelId;
    private String writer;
    private String content;
    private Long timestamp;
}