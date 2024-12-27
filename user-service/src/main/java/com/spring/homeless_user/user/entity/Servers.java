package com.spring.homeless_user.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "servers", indexes = {
        @Index(name = "idx_servers_user_id", columnList = "user_id"),
        @Index(name = "idx_servers_server_id", columnList = "serverId")
})
public class Servers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer serverId;

    @Enumerated(EnumType.STRING)
    private AddStatus addStatus;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}