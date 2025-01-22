package com.playdata.homelesscode.dto.server;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@ToString
@Builder
public class ServerEditDto {

    private String id;
    private String title;
    private String tag;
    private MultipartFile serverImg;

}
