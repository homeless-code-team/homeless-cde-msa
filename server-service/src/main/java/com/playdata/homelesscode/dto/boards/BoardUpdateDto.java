package com.playdata.homelesscode.dto.boards;


import com.playdata.homelesscode.entity.Board;
import com.playdata.homelesscode.entity.Server;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardUpdateDto {

    private String boardId;
    private String boardTitle;



}
