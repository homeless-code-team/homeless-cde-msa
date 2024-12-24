package com.playdata.homelesscode.dto.board;


import com.playdata.homelesscode.entity.Board;
import com.playdata.homelesscode.entity.Channel;
import com.playdata.homelesscode.entity.Server;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCreateDto {


    private String boardTitle;

    private String channelId;

    private String writer;
    private String tag;

    public Board toEntity(Channel channel) {
        return Board.builder()
                .boardTitle(boardTitle)
                .tag(tag)
                .writer(writer)
                .channel(channel)
                .build();
    }
}
