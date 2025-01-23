package com.homeless.chatservice.dto;

import lombok.Builder;

// 메세지 전송시 킇라이언트 -> 서버 보내는 데이터 객체
// 작성자와 메세지 내용을 보냄.
@Builder
public record ChatMessageRequest(String serverId,
                                 String email,
                                 String writer,
                                 String content,
                                 MessageType messageType,
                                 String fileUrl,
                                 String fileName) {

}

