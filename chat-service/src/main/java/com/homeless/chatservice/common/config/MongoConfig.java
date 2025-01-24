package com.homeless.chatservice.common.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@ConfigurationProperties(prefix = "spring.data.mongodb")
@Slf4j
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String uri; // MongoDB URI 주입

    @Override
    @NonNull
    protected String getDatabaseName() {
        return "chat"; // 사용할 데이터베이스 이름
    }

    @Override
    @NonNull
    public MongoClient mongoClient() {
        log.info("Connecting to MongoDB: {}", uri); // MongoDB URI 로그
        return MongoClients.create(uri); // URI를 사용하여 MongoClient 생성
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName()); // MongoTemplate 생성
    }


}