package com.playdata.homelesscode.common.utill;


import com.playdata.homelesscode.common.dto.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
public class SecurityContextUtil {

    public static CustomUserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal) {
            return (CustomUserPrincipal) authentication.getPrincipal();
        }
        throw new IllegalStateException("No authenticated user found");
    }


}
