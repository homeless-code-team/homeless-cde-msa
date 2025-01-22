package com.playdata.homelesscode.dto.boards;


import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCreateDto {

    private String title;
    private String boardListId;


}
