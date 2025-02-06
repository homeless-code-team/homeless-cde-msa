package com.spring.homeless_user.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_nickname", columnList = "nickname")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36) // UUID 길이 설정 (최대 36자)
    private String id;

    @Column(unique = true, nullable = false, length = 100) // 이메일은 100자 제한 (충분한 길이)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20) // Provider Enum의 최대 길이를 고려하여 20자로 제한
    private Provider provider;

    @Column(nullable = false, length = 255) // 비밀번호는 보통 해싱되므로 255자로 설정
    private String password;

    @Column(nullable = false, length = 8) // 닉네임 길이 제한 (8자)
    private String nickname;

    @Column(length = 255) // 프로필 이미지 URL (최대 길이 255)
    private String profileImage;

    @Column(length = 1500) // 소개글 (500자까지 가능)
    private String contents;

    @Column(length = 10) // 성취 항목 (10자까지 가능)
    private String achievement;

    @Column(nullable = false, updatable = false) // 생성 시점 수정 불가
    private LocalDateTime createdAt;
}
