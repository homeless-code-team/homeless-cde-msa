package com.playdata.homelesscode.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@Entity
@Table(name = "tbl_board")
@DynamicUpdate
public class Board {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)

    private String id;
    private String title;
    private String writer;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_list_id", nullable = false)
    @JsonIgnore
    private BoardList boardList;

    @Column
    @CreationTimestamp
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm", timezone="Asia/Seoul") //날짜 포멧 바꾸기
    private String createAt;



}