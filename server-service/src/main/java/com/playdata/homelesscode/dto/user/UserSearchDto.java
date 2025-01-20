package com.playdata.homelesscode.dto.user;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchDto {

    private String id;
    private String searchName;
}
