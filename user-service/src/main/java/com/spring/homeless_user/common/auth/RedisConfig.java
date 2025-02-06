package com.spring.homeless_user.common.auth;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@RequiredArgsConstructor
@Configuration
@EnableCaching
@Slf4j
public class RedisConfig {


    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;


    // 0번 DB RedisTemplate
    @Bean(name = "check")
    public RedisTemplate<String, String> checkTemplate() {
        return createRedisTemplate(0);
    }

    // 1번 DB RedisTemplate
    @Bean(name = "login")
    public RedisTemplate<String, String> loginTemplate() {
        return createRedisTemplate(1);
    }

    // 2번 DB RedisTemplate
    @Bean(name = "friends")
    public RedisTemplate<String, String> friendsTemplate() {
        return createRedisTemplate(2);
    }

    // 3번 DB RedisTemplate
    @Bean(name = "server")
    public RedisTemplate<String, String> serverTemplate() {
        return createRedisTemplate(3);
    }

    // 10번 DB RedisTemplate
    @Bean(name = "cache")
    public RedisTemplate<String, String> cacheTemplate() {
        return createRedisTemplate(10);
    }

    // 공통 RedisTemplate 생성 로직
    private RedisTemplate<String, String> createRedisTemplate(int dbIndex) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        log.info("레디스 host: {}", host);
        log.info("레디스 port: {}", port);
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(dbIndex);

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration);
        connectionFactory.afterPropertiesSet();

        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }

    // Redis 10번 DB ConnectionFactory (캐싱용)
    @Bean(name = "cacheConnectionFactory")
    public LettuceConnectionFactory cacheConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(10); // Redis DB 10번 지정
        return new LettuceConnectionFactory(configuration);
    }

    // RedisCacheManager 설정 (10번 DB 전용)
    @Bean
    public RedisCacheManager cacheManager(@Qualifier("cacheConnectionFactory") LettuceConnectionFactory cacheConnectionFactory) {
        // ObjectMapper 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Java 8 날짜/시간 모듈
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // RedisCacheConfiguration 생성
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // 캐시 TTL 설정
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper))
                );

        // RedisCacheManager 생성
        cacheConnectionFactory.afterPropertiesSet();
        return RedisCacheManager.builder(cacheConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
}
