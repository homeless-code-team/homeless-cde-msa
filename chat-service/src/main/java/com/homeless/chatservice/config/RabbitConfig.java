package com.homeless.chatservice.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
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


// ReadOnly attribute

@Configuration
@EnableRabbit // Spring에서 RabbitMQ 기능 활성화 하기
@Slf4j
public class RabbitConfig {

    private final String CHAT_QUEUE_NAME;
    private final String CHAT_EXCHANGE_NAME;
    private final String CHAT_ROUTING_KEY;
    private final String RABBITMQ_HOST;

    public RabbitConfig(
            @Value("${rabbitmq.chat-queue.name}") String CHAT_QUEUE_NAME,
            @Value("${rabbitmq.chat-exchange.name}") String CHAT_EXCHANGE_NAME,
            @Value("${rabbitmq.chat-routing.key}") String CHAT_ROUTING_KEY,
            @Value("${spring.rabbitmq.host}") String RABBITMQ_HOST
    ) {
        this.CHAT_QUEUE_NAME = CHAT_QUEUE_NAME;
        this.CHAT_EXCHANGE_NAME = CHAT_EXCHANGE_NAME;
        this.CHAT_ROUTING_KEY = CHAT_ROUTING_KEY;
        this.RABBITMQ_HOST = RABBITMQ_HOST;
    }

    // "chat.queue"라는 이름의 Queue 생성
    // 실제로 메시지가 저장되는 공간인 Queue.
    @Bean
    public Queue chatQueue() {
        log.info("Creating queue: {}", CHAT_QUEUE_NAME);
        return new Queue(CHAT_QUEUE_NAME, true); // durable을 true로 세팅해서 지속성 주기
    }

    // 메시지를 큐로 라우팅 해 주는 역할인 Exchange 생성
    // 4가지 Binding 전략 중 TopicExchange 전략을 사용. "chat.exchange"를 이름으로 지정
    @Bean
    public TopicExchange chatExchange() {
        log.info("Creating exchange: {}", CHAT_EXCHANGE_NAME);
        return new TopicExchange(CHAT_EXCHANGE_NAME);
    }

    // Exchange와 Queue를 연결.
    // 라우팅 키 패턴을 통해 어떤 메세지가 어떤 큐로 갈 지를 결정하게 된다.
    // "chat.queue"에 "chat.exchange" 규칙을 Binding
    @Bean
    public Binding chatBinding() {
        log.info("Creating binding between: {} and {}", CHAT_QUEUE_NAME, CHAT_EXCHANGE_NAME);
        return BindingBuilder
                .bind(chatQueue())
                .to(chatExchange())
                .with(CHAT_ROUTING_KEY);
    }

    // RabbitMQ로 메시지를 주고받는 핵심 클래스
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }



    // 스프링의 메시지 추상화를 RabbitMQ에 적용. 좀 더 높은 수준의 메시지 기능을 제공.
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

    // Queue를 구독(Subscribe)하는 걸 어떻게 처리하느냐에 따라 필요함. 당장은 없어도 됨.
    // 나중에 @RabbitListener 사용할 때 필요한 설정을 여기에서 지정.
    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
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
        log.info("CHAT_QUEUE_NAME: {}", CHAT_QUEUE_NAME);
        log.info("CHAT_EXCHANGE_NAME: {}", CHAT_EXCHANGE_NAME);
        log.info("CHAT_ROUTING_KEY: {}", CHAT_ROUTING_KEY);
        log.info("RABBITMQ_HOST: {}", RABBITMQ_HOST);
    }


    // 컨텍스트 초기화 후 큐와 익스체인지 선언
    @EventListener(ContextRefreshedEvent.class)
    public void initialize(ContextRefreshedEvent event) {
        log.info("Initializing RabbitMQ exchanges and queues...");

        RabbitAdmin admin = event.getApplicationContext().getBean(RabbitAdmin.class);

        try {
            // Exchange 선언
            TopicExchange exchange = new TopicExchange(CHAT_EXCHANGE_NAME, true, false);
            admin.declareExchange(exchange);
            log.info("Declared exchange: {}", CHAT_EXCHANGE_NAME);

            // Queue 선언
            Queue queue = new Queue(CHAT_QUEUE_NAME, true);
            admin.declareQueue(queue);
            log.info("Declared queue: {}", CHAT_QUEUE_NAME);

            Binding binding = BindingBuilder
                    .bind(queue)
                    .to(exchange)
                    .with(CHAT_ROUTING_KEY);
            admin.declareBinding(binding);
            log.info("Declared binding between {} and {} with routing key {}",
                    CHAT_QUEUE_NAME, CHAT_EXCHANGE_NAME, CHAT_ROUTING_KEY);


        } catch (Exception e) {
            log.error("Error during RabbitMQ initialization", e);
            throw e;
        }
    }

    // 메시지 전송 메서드
    public void sendMessageToQueue(String routingKey, Object message, RabbitTemplate rabbitTemplate) {
        log.info("Sending message to queue with routingKey: {}", routingKey);
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, routingKey, message);
    }

}
