package com.playdata.homelesscode.dto.boards;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardSearchDto {

    private String id;
    private String searchName;

}
