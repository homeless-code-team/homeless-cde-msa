package com.homeless.chatservice.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;


@Configuration
@EnableRabbit // Spring에서 RabbitMQ 기능 활성화 하기
@Slf4j
public class RabbitConfig {

    private final String CHAT_EXCHANGE_NAME;
    private final String RABBITMQ_HOST;

    public RabbitConfig(
            @Value("${rabbitmq.chat-exchange.name}") String CHAT_EXCHANGE_NAME,
            @Value("${spring.rabbitmq.host}") String RABBITMQ_HOST
    ) {
        this.CHAT_EXCHANGE_NAME = CHAT_EXCHANGE_NAME;
        this.RABBITMQ_HOST = RABBITMQ_HOST;
    }

    // "chat.queue"라는 이름의 Queue 생성
    // 실제로 메시지가 저장되는 공간인 Queue.
    // Bean으로 등록하지 않고, 채널이 생성될 때 직접 호출할 예정이다.
    public Queue createChatQueue(String channelId) {
        String queueName = "chat.channel." + channelId;
        log.info("Creating queue: {}", queueName);
        return new Queue(queueName, true); // durable을 true로 세팅해서 지속성 주기
    }

    // 메시지를 큐로 라우팅 해 주는 역할인 Exchange 생성
    // 4가지 Binding 전략 중 TopicExchange 전략을 사용. "chat.exchange"를 이름으로 지정
    @Bean
    public TopicExchange chatExchange() {
        log.info("Creating exchange: {}", CHAT_EXCHANGE_NAME);
        return new TopicExchange(CHAT_EXCHANGE_NAME);
    }


    // Binding 생성 메서드 (빈 등록하지 않고 동적 생성으로 변경)
    public Binding createChatChannelBinding(Queue queue, String channelId) {
        return BindingBuilder
                .bind(queue)
                .to(chatExchange())
                .with("chat.channel." + channelId);
    }

    // RabbitMQ로 메시지를 주고받는 핵심 클래스
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }


    // 스프링의 메시지 추상화를 RabbitMQ에 적용. 좀 더 높은 수준의 메시지 기능을 제공.
    // 반드시 필요한 것은 아님. 기본 RabbitTemplate 사용해도 괜춘.
    @Bean
    public RabbitMessagingTemplate rabbitMessagingTemplate(RabbitTemplate rabbitTemplate) {
        return new RabbitMessagingTemplate(rabbitTemplate);
    }

    // RabbitMQ서버와 연결 설정. CachingConnectionFactory를 선택해서 연결 캐싱 및 성능 향상
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setUsername("guest"); // RabbitMQ 관리자 아이디
        factory.setPassword("guest"); // RabbitMQ 관리자 비밀번호
        factory.setPort(5672); // RabbitMQ 연결할 port
        factory.setVirtualHost("/"); // vhost 지정

        return factory;
    }

    /**
     * 직렬화(메세지를 JSON 으로 변환하는 Message Converter)
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitAdmin -> RabbitMQ 관리 작업을 수행하는 클래스
    // 큐, 익스체인지, 바인딩 등을 생성하고 관리할 수 있게 해 주는 클래스. (얘가 있어야 큐, 익스체인지가 생성됨)
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);  // 자동 시작 설정
        return admin;
    }

    // 단순 로그 확인용.
    @PostConstruct
    public void checkConfiguration() {
        log.info("Checking RabbitMQ configuration...");
        log.info("CHAT_EXCHANGE_NAME: {}", CHAT_EXCHANGE_NAME);
        log.info("RABBITMQ_HOST: {}", RABBITMQ_HOST);
    }


    // 스프링 컨텍스트가 완전히 로드가 된 이후 발생하는 이벤트 리스너 메서드.
    // 모든 컴포넌트를 명시적으로 선언. 이미 존재하는 경우에는 무시하고 존재하지 않을 때에만 생성.
    // 좀 더 확실하게 RabbitMQ 자원들을 생성하기 위해 추가함.
    @EventListener(ContextRefreshedEvent.class)
    public void initialize(ContextRefreshedEvent event) {
        log.info("Initializing RabbitMQ exchanges and queues...");

        RabbitAdmin admin = event.getApplicationContext().getBean(RabbitAdmin.class);

        try {
            // exchange 선언
            TopicExchange exchange = new TopicExchange(CHAT_EXCHANGE_NAME, true, false);
            admin.declareExchange(exchange);
            log.info("Declared exchange: {}", CHAT_EXCHANGE_NAME);

        } catch (Exception e) {
            log.error("Error during RabbitMQ initialization", e);
            throw e;
        }
    }



}
