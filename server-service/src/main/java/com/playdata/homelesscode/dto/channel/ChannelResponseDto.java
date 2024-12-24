package com.playdata.homelesscode.dto.channel;

import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelResponseDto {

    private String name;
    private String createAt;


    public static String makeDateStringFomatter(LocalDateTime createAt) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String format = dtf.format(createAt);

        return format;
    }
}
