package com.spring.homelesscode.friends_server.common.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {

    @Bean(name = "check")
    public RedisTemplate<String, String> checkTemplate() {
        return createRedisTemplate(0); // Database 0
    }

    @Bean(name = "login")
    public RedisTemplate<String, String> loginTemplate() {
        return createRedisTemplate(1); // Database 1
    }

    @Bean(name = "friends")
    public RedisTemplate<String, String> friendsTemplate() {
        return createRedisTemplate(2); // Database 2
    }

    @Bean(name = "server")
    public RedisTemplate<String, String> serverTemplate() {
        return createRedisTemplate(3); // Database 3
    }

    private RedisTemplate<String, String> createRedisTemplate(int dbIndex) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName("localhost");
        configuration.setPort(6379);
        configuration.setDatabase(dbIndex); // 설정한 DB Index 사용

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
}
