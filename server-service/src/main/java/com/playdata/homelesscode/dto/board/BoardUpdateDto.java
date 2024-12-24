package com.playdata.homelesscode.dto.board;


import com.playdata.homelesscode.entity.Board;
import com.playdata.homelesscode.entity.Channel;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardUpdateDto {

    private String id;
    private String boardTitle;
    private String tag;
    private String channelId;
    private String writer;


    public Board toEntity(Channel channel) {
        return Board.builder()
                .id(id)
                .boardTitle(boardTitle)
                .tag(tag)
                .writer(writer)
                .channel(channel)
                .build();
    }

}
