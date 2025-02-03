package com.spring.homeless_user.user.service;

import com.spring.homeless_user.common.auth.JwtTokenProvider;
import com.spring.homeless_user.user.dto.CommonResDto;
import com.spring.homeless_user.user.dto.EmailCheckDto;
import com.spring.homeless_user.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CheckService {

    private final JavaMailSender mailSender;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> checkTemplate;

    public CheckService(JavaMailSender mailSender,
                        JwtTokenProvider jwtTokenProvider,
                        UserRepository userRepository,
                        @Qualifier("check") RedisTemplate<String, String> checkTemplate) {
        this.mailSender = mailSender;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.checkTemplate = checkTemplate;
    }

    // 인증 이메일 전송 로직 인증번호 10분 유효 (이메일 만 필요)
    public CommonResDto sendVerificationEmail(EmailCheckDto dto) {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sendEmail", "/api/v1/users/confirm", "POST"));
        links.add(new CommonResDto.Link("checkEmail", "/api/v1/users/confrim", "GET"));

        String token = jwtTokenProvider.emailToken(dto.getEmail());

        String subject = "이메일 인증을 해주세요";
        String text = "<p>이메일 인증을 완료하려면 아래 인증번호를 입력창에 입력해주세요:</p>" +
                "<h3>" + token + "</h3>";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(dto.getEmail());
            helper.setSubject(subject);
            helper.setText(text, true);

            mailSender.send(message);

            checkTemplate.opsForValue().set(token, dto.getEmail(), Duration.ofMinutes(10));


            return new CommonResDto(HttpStatus.OK, 200, "이메일 전송 성공!!!", token, links);
        } catch (Exception e) {
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 401, "이메일 전송 실패", null, links);
        }
    }

    // 이메일 인증, 비밀번호 회원가입시
    public CommonResDto confirm(EmailCheckDto dto) {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sendEmail", "/api/v1/users/confirm", "POST"));
        links.add(new CommonResDto.Link("checkEmail", "/api/v1/users/confrim", "GET"));
        try {

            String token = dto.getToken();
            String email = dto.getEmail();

            boolean equals = Boolean.TRUE.equals(checkTemplate.hasKey(token));
            boolean flag = userRepository.findByEmail(email).isPresent();
            if (equals) {
                if (flag) {
                    checkTemplate.delete(token);
                    return new CommonResDto(HttpStatus.OK, 200, "token 유효, 비밀번호를 수정해주세요", null, links);
                } else {
                    checkTemplate.delete(token);
                    return new CommonResDto(HttpStatus.OK, 200, "token 유효, 회원가입을 계속 진행하세요", null, links);
                }
            }

            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "토큰이 저장되지 않았습니다", null, links);
        } catch (Exception e) {
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "에러발생" + e.getMessage(), null, links);
        }
    }

    // 이메일 &닉네임 중복검사
    public CommonResDto duplicateCheck(String email, String nickname) {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("duplicate", "/api/v1/users/duplicate", "GET"));
        links.add(new CommonResDto.Link("sign-up", "/api/v1/users/sign-up", "POST"));
        links.add(new CommonResDto.Link("modify", "/api/v1/users", "PATCH"));
        try {
            // 이메일 중복 체크
            if (email != null) {
                boolean exists = userRepository.findByEmail(email).isPresent();
                return exists
                        ? new CommonResDto(HttpStatus.BAD_REQUEST, 401, "이메일 사용 불가", null, links)
                        : new CommonResDto(HttpStatus.OK, 200, "이메일을 사용해도 좋아요.", null, links);
            } else if (nickname != null) {
                //닉네임 중복체크
                boolean nicknameExists = userRepository.findByNickname(nickname).isPresent();
                return nicknameExists
                        ? new CommonResDto(HttpStatus.BAD_REQUEST, 400, "닉네임 사용 불가", null, links)
                        : new CommonResDto(HttpStatus.OK, 200, "닉네임을 사용해도 좋아요.", null, links);
            }

            // 이메일과 닉네임 모두 null인 경우
            return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "잘못된 요청입니다.", null, links);

        } catch (Exception e) {
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 401, "에러 발생", e.getMessage(), links);
        }
    }

}
