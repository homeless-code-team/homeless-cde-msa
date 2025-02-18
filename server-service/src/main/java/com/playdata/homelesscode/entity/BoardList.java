package com.playdata.homelesscode.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
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
@Table(name = "tbl_boardList")
@DynamicUpdate
public class BoardList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    @JsonIgnore
    private Server server;

    @Column
    private String boardTitle;
    @Column
    private String writer;
    @Column
    private String tag;


    @OneToMany(mappedBy = "boardList", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Board> boards;

    @Column
    @CreationTimestamp
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm", timezone="Asia/Seoul") //날짜 포멧 바꾸기
    private LocalDateTime createAt;


    public void setBoard(BoardList board){
        this.boardTitle = board.getBoardTitle();
        this.writer = board.getWriter();
        this.tag = board.getTag();


    }

}