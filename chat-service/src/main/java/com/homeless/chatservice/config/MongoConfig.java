package com.homeless.chatservice.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@ConfigurationProperties(prefix = "spring.data.mongodb")
public class MongoConfig extends AbstractMongoClientConfiguration {

    private String uri; // 이 필드에 값을 주입받습니다.

    @Override
    protected String getDatabaseName() {
        return "chat"; // 사용할 데이터베이스 이름
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(uri); // uri를 사용하여 MongoClient를 생성
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName()); // mongoClient와 db 이름을 통해 MongoTemplate을 생성
    }

    // Getter and Setter for uri (Spring이 자동으로 주입할 수 있도록)
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
