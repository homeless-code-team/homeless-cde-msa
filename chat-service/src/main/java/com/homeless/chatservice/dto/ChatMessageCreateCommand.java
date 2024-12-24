package com.homeless.chatservice.dto;

import lombok.Builder;

// 채팅 메세지 생성시 필요한 데이터를 가지고있는 dto 객체
// 채널id, 내용, 작성자
@Builder
public record ChatMessageCreateCommand(Long serverId,
                                       Long channelId, String content, String writer) {

}
