package com.spring.homeless_user.user.dto;

import com.spring.homeless_user.user.entity.AddStatus;
import lombok.Data;

@Data
public class ServerDto {
    private String ReqEmail;// 서버 가입을 하려는 사용자
    private String ResEmail;// 서버 가입을 시키려는 서버 주인
    private int serverId;
    private AddStatus addStatus;
}
