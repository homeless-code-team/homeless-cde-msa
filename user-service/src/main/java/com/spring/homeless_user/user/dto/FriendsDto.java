package com.spring.homeless_user.user.dto;

import com.spring.homeless_user.user.entity.AddStatus;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendsDto {
    private String resEmail;
    private AddStatus addStatus;
}
