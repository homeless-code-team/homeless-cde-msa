package com.playdata.homelesscode.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@Entity
@Table(name = "tbl_servers")
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="user_id")
//    @JsonIgnore
//    private User user;

    private String email;

    @Column
    private String title;

    @Column
    private String tag;

    @Column
    private String serverImg;

    @Column
    private int serverType;

    @Column
    @CreationTimestamp
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm", timezone="Asia/Seoul") //날짜 포멧 바꾸기
    private LocalDateTime createAt;

    @OneToMany(mappedBy = "server", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Channel> channels;

    @OneToMany(mappedBy = "server", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ServerJoinUserList> serverLists;

    @OneToMany(mappedBy = "server", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BoardList> board;

}
