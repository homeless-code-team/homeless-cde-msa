package com.homeless.chatservice.common.interceptor;

import com.homeless.chatservice.common.auth.JwtUtils;
import com.homeless.chatservice.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            // CONNECT 및 SEND 요청에서만 처리
            if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                    StompCommand.SEND.equals(accessor.getCommand())) {

                // 1. Authorization 헤더 추출
                String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
                log.info("Authorization header: {}", authorizationHeader);

                // 2. 토큰 검증
                if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                    log.error("Missing or invalid Authorization header");
                    throw new UnauthorizedException("401","Missing or invalid Authorization header");
                }

                // "Bearer " 제거
                String token = authorizationHeader.substring(7);

                try {
                    // 3. JWT 토큰 유효성 검사
                    jwtUtils.validateToken(token);

                    // 4. 토큰에서 이메일 추출
                    String email = jwtUtils.getEmailFromToken(token);
                    log.info("Authenticated user: {}", email);

                    // 5. 사용자 정보를 STOMP 세션에 추가
                    accessor.setUser(() -> email);
                } catch (Exception e) {
                    log.error("Token validation failed: {}", e.getMessage());
                    throw new UnauthorizedException("401","Invalid token: " + e.getMessage());
                }
            }
        }

        return message;
    }


    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            log.info("Post-send processing for message: {}", message);
        }
    }
}
