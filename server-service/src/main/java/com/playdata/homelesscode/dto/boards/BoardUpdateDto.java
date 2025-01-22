package com.playdata.homelesscode.dto.boards;


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
