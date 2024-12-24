package com.playdata.homelesscode.controller;

import com.playdata.homelesscode.dto.server.ServerCreateDto;
import com.playdata.homelesscode.entity.Server;
import com.playdata.homelesscode.entity.User;
import com.playdata.homelesscode.repository.ServerListRepository;
import com.playdata.homelesscode.repository.ServerRepository;
import com.playdata.homelesscode.repository.UserRepository;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
class ServerControllerTest {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerListRepository serverListRepository;


    @Test
    @DisplayName("서버 생성")
    @Transactional
    @Rollback(false)
    void createServer() {
        // given
        User user = User.builder()
                .email("maru@naver.com")
                .password("qwer1234")
                .nickName("마루")
                .loginMethod(null)
                .markdown(null)
                .achievement(null)
                .build();
        User saveUser = userRepository.save(user);


        ServerCreateDto dto = ServerCreateDto.builder()
                .title("테스트 제목2")
                .tag("#테스트 태그2")
                .chatingRoom(null)
                .serverImg(null)
                .build();
        Server server = dto.toEntity(saveUser);
        // when

        Server save = serverRepository.save(server);

        // then

        System.out.println(save);
    }

}