package com.playdata.homelesscode.dto.boardList;


import com.playdata.homelesscode.entity.BoardList;
import com.playdata.homelesscode.entity.Server;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardListCreateDto {


    private String boardTitle;
    private String serverId;
    private String tag;

    public BoardList toEntity(Server server) {
        return BoardList.builder()
                .boardTitle(boardTitle)
                .tag(tag)
                .server(server)
                .build();
    }
}
