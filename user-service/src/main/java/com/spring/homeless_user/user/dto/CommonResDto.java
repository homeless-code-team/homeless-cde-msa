package com.spring.homeless_user.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class CommonResDto {
    private HttpStatus status;
    private int code;
    private String message;
    private Object data;
    private List<Link> links;
    @Data
    @AllArgsConstructor
    public static class Link {
        private String rel; // 링크의 관계 (예: self, login)
        private String href; // 링크 URL
        private String method; // HTTP 메서드 (GET, POST 등)
    }
}
