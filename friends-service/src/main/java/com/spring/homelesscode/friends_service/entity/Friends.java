package com.spring.homelesscode.friends_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_friends", indexes = {
        @Index(name = "idx_user_email", columnList = "user_email"),
        @Index(name = "idx_friend_email", columnList = "friend_email"),
})
public class Friends {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_email")
    private String senderEmail;

    @Column(name = "friend_email")
    private String receiverEmail;

    @Enumerated(EnumType.STRING)
    private AddStatus status;

    @Column(name = "createAt")
    private DateTime createAt;

}
