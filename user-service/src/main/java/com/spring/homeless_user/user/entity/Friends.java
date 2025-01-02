package com.spring.homeless_user.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_friends", indexes = {
        @Index(name = "idx_friends_user_email", columnList = "user_email"),
        @Index(name = "idx_friends_friend_email", columnList = "friend_email")
})
public class Friends {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_email", referencedColumnName = "email", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_email", referencedColumnName = "email", nullable = false)
    private User friend;

    public Friends(User user, User friend) {
        this.user = user;
        this.friend = friend;
    }

}
