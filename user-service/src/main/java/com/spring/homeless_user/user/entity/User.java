package com.spring.homeless_user.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import java.time.LocalDateTime;
import java.util.ArrayList;
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    private String nickname;
    private String profileImage;
    private String MarkDown;
    private String Achievement;
    private String LoginMethod;
    private LocalDateTime CreateAt;
    private String refreshToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friends> friends = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Servers> serverList = new ArrayList<>();
}
