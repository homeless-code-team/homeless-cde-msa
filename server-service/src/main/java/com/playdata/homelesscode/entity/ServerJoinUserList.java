package com.playdata.homelesscode.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.playdata.homelesscode.dto.server.Role;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@Entity
@Table(
        name = "tbl_server_join_user_list",
        indexes = @Index(name = "idx_server_join_user_list_email", columnList = "email")
)
public class ServerJoinUserList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="server_id", nullable = false)
    @JsonIgnore
    private Server server;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="user_id", nullable = false)
//    @JsonIgnore
//    private User user;

    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;
}