package com.playdata.homelesscode.dto.boards;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDeleteDto {

    private String serveId;
    private String boardId;
}
