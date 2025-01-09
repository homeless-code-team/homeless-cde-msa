package com.spring.homelesscode.friends_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_friends", indexes = {
        @Index(name = "idx_user_nickname", columnList = "user_nickname"),
})
public class Friends {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "user_nickname")
    private String nickname;

    @Column(name = "friends_nickname")
    private String nicknames;

    private AddStatus addStatus;
}
