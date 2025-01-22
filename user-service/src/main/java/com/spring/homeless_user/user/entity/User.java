package com.spring.homeless_user.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
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
    private String  id;

    @Column(unique = true, nullable = false)
    private String email;

    private Provider provider;
    private String password;
    private String nickname;
    private String profileImage;
    private String contents;
    private String achievement;
    private LocalDateTime createdAt;
    private String refreshToken;

}
