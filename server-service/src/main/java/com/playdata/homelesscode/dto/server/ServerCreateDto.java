package com.playdata.homelesscode.dto.server;

import com.playdata.homelesscode.entity.Server;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@ToString
@Builder
public class ServerCreateDto {


    private String title;
    private String tag;
    private MultipartFile serverImg;



    public Server toEntity() {
        return Server.builder()
                .title(title)
                .tag(tag)
                .build();
    }

}
