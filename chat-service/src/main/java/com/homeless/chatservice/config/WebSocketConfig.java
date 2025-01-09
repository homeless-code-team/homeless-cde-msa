package com.homeless.chatservice.config;
import com.homeless.chatservice.Listener.WebSocketEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.*;
import reactor.netty.tcp.TcpClient;

// ReadOnly

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketEventListener webSocketEventListener;

    @Value("${spring.rabbitmq.host}")
    private String RABBITMQ_HOST;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("registerStompEndpoints called!");
        // 모든 출처 허용
        registry.addEndpoint("/ws") // WebSocket 엔드포인트
                .setAllowedOriginPatterns("*") // CORS 설정
                .withSockJS(); // SockJS 활성화
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 비동기 TCP 클라이언트를 설정하여 RabbitMQ와 연결
        TcpClient tcpClient = TcpClient.create()
                .host(RABBITMQ_HOST)
                .port(61613); // STOMP 포트

        ReactorNettyTcpClient<byte[]> client = new ReactorNettyTcpClient<>(tcpClient, new StompReactorNettyCodec());

        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
                .setAutoStartup(true)
                .setTcpClient(client)
                .setRelayHost(RABBITMQ_HOST)
                .setRelayPort(61613)
                .setSystemLogin("guest")
                .setSystemPasscode("guest")
                .setClientLogin("guest")
                .setClientPasscode("guest");

        registry.setPathMatcher(new AntPathMatcher("."));
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.addDecoratorFactory(webSocketEventListener);
    }
}
