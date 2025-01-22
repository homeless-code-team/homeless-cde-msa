package com.homeless.chatservice.common.config;

import com.homeless.chatservice.common.interceptor.StompInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import reactor.netty.tcp.TcpClient;

@Configuration
@EnableWebSocketMessageBroker // WebSocket을 통한 메시지 브로커 기능 활성화하기
@Slf4j
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompInterceptor stompInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // STOMP 인터셉터 추가
        registration.interceptors(stompInterceptor);
    }
    @Value("${spring.rabbitmq.host}")
    private String RABBITMQ_HOST;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("registerStompEndpoints called!");
        // stomp 접속 주소 url = ws://localhost:8181/ws
        registry.addEndpoint("/ws") // 연결될 엔드포인트
                .setAllowedOriginPatterns("*")
                .withSockJS(); // WebSocket을 지원하지 않는 브라우저를 위한 옵션
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 비동기 TCP 클라이언트를 따로 구성해 준다. (나중에 보안 연결을 위해서도 필요함.)
        TcpClient tcpClient = TcpClient
                .create()
                .host(RABBITMQ_HOST)
                .port(61613); // STOMP 프로토콜의 기본 포트번호는 61613 이다.

        ReactorNettyTcpClient<byte[]> client = new ReactorNettyTcpClient<>(tcpClient, new StompReactorNettyCodec());

        // Spring 내장 메세지 브로커 대신 외부 메세지 브로커를 사용하도록 지시하는 설정. 고정값이라 변경 안됨! (RabbitMQ에서 정한 값)
        /*
        /queue: point-to-point 메시징을 보낼 때 사용
        /topic: 발행/구독(pub/sub) 메시징에 사용
        /exchange: RabbitMQ의 exchange를 직접 지정할 때 사용
        /amq/queue: RabbitMQ의 특정 큐에 직접 메시지를 보낼 때 사
         */

        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
                .setAutoStartup(true)
                .setTcpClient(client) // RabbitMQ와 연결할 클라이언트 설정
                .setRelayHost(RABBITMQ_HOST) // RabbitMQ 서버 주소
                .setRelayPort(61613) // RabbitMQ 포트(5672), STOMP(61613)
                .setSystemLogin("guest") // RabbitMQ 시스템 계정
                .setSystemPasscode("guest") // RabbitMQ 시스템 비밀번호
                .setClientLogin("guest") // RabbitMQ 클라이언트 계정
                .setClientPasscode("guest"); // RabbitMQ 클라이언트 비밀번호

        registry.setPathMatcher(new AntPathMatcher(".")); // url을 chat/room/3 -> chat.room.3으로 참조하기 위한 설정
        registry.setApplicationDestinationPrefixes("/pub"); // 클라이언트에서 메시지 발행 시 사용할 접두어를 pub으로 세팅.


        // 메시지를 구독(수신)하는 요청 엔드포인트
//        registry.enableSimpleBroker("/sub");

        // 메시지를 발행(송신)하는 엔드포인트
//        registry.setApplicationDestinationPrefixes("/pub");
    }

}