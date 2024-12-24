package com.homeless.chatservice.dto;


// 메세지 전송시 킇라이언트 -> 서버 보내는 데이터 객체
// 작성자와 메세지 내용을 보냄.
public record ChatMessageRequest(String writer, String text, Long timestamp) {

}
