package com.playdata.homelesscode.dto.server;

import com.playdata.homelesscode.entity.Server;
import com.playdata.homelesscode.entity.User;
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

    private String userId;
    private String title;
    private String tag;
    private MultipartFile serverImg;



    public Server toEntity(User user) {
        return Server.builder()
                .title(title)
                .tag(tag)
                .serverImg(serverImg.getOriginalFilename())
                .user(user)
                .build();
    }

}
