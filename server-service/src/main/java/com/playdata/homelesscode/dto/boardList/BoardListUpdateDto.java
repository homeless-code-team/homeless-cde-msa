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
public class BoardListUpdateDto {

    private String id;
    private String boardTitle;
    private String tag;
    private String serverId;


    public BoardList toEntity(Server server) {
        return BoardList.builder()
                .id(id)
                .boardTitle(boardTitle)
                .tag(tag)
                .server(server)
                .build();
    }

}
