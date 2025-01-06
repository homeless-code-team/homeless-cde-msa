package com.spring.homeless_user.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_server", indexes = {
        @Index(name = "idx_servers_user_id", columnList = "user_id"),
        @Index(name = "idx_servers_server_id", columnList = "serverId")
})
public class Servers {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private int serverId;

    @Enumerated(EnumType.STRING)
    private AddStatus addStatus;

    @ManyToOne
    @JoinColumn(name = "user_email", nullable = false)
    @JsonIgnore
    private User user; //1234




}