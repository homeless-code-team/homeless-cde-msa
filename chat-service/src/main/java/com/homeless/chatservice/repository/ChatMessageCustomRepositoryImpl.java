package com.homeless.chatservice.repository;

import com.homeless.chatservice.entity.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class ChatMessageCustomRepositoryImpl implements ChatMessageCustomRepository{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void updateContent(String chatId, String content) {
        // ID로 메시지를 찾는 쿼리 객체 생성
        Query query = new Query(Criteria.where("id").is(chatId));  // Criteria를 사용하여 조건 추가

        // 업데이트할 내용 설정
        Update update = new Update();
        update.set("content", content);  // content 필드를 새로운 내용으로 설정

        // MongoTemplate을 이용해 첫 번째 일치하는 메시지만 업데이트
        mongoTemplate.updateFirst(query, update, ChatMessage.class);
        // 조건에 맞는 순서가 없을시 예외 처리
        if (mongoTemplate.updateFirst(query, update, ChatMessage.class).getMatchedCount() == 0) {
            throw new IllegalArgumentException("Message not found for the given chatId.");
        }
    }
}
