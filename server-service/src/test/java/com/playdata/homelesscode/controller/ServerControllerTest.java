package com.playdata.homelesscode.controller;

import com.playdata.homelesscode.repository.ServerJoinUserListRepository;
import com.playdata.homelesscode.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class ServerControllerTest {

    @Autowired
    private ServerRepository serverRepository;

//    @Autowired
//    private UserRepository userRepository;

    @Autowired
    private ServerJoinUserListRepository serverListRepository;


//    @Test
//    @DisplayName("서버 생성")
//    @Transactional
//    @Rollback(false)
//    void createServer() {
//        // given
//        User user = User.builder()
//                .email("maru@naver.com")
//                .password("qwer1234")
//                .nickName("마루")
//                .loginMethod(null)
//                .markdown(null)
//                .achievement(null)
//                .build();
//        User saveUser = userRepository.save(user);
//
//
//        ServerCreateDto dto = ServerCreateDto.builder()
//                .title("테스트 제목2")
//                .tag("#테스트 태그2")
//                .serverImg(null)
//                .build();
//        Server server = dto.toEntity(saveUser);
//        // when
//
//        Server save = serverRepository.save(server);
//
//        // then
//
//        System.out.println(save);
//    }

}

//INSERT INTO tb_user (id, email, password, nick_name, profile_img, created_at, markdown, achievement, login_method)
//VALUES
//        (UUID(), 't1@0.com', '1234', '테스트유저1', null, NOW(), NULL, NULL, 'local'),
//        (UUID(), 't2@0.com', '1234', '테스트유저2', null, NOW(), NULL, NULL, 'google'),
//        (UUID(), 'user789@example.com', 'password789', 'User789', 'https://example.com/user789.png', NOW(), NULL, NULL, 'facebook');