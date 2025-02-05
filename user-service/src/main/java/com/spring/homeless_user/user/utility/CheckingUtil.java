package com.spring.homeless_user.user.utility;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CheckingUtil {
    // 비밀번호 유효성 검사 정규식
    private final String PASSWORD_PATTERN =
            "^(?=.*[0-9])" +                 // 적어도 1개의 숫자
                    "(?=.*[a-z])" +                  // 적어도 1개의 소문자
                    "(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~])" + // 적어도 1개의 특수문자
                    "(?=\\S+$).{8,}$";             // 8자 이상의 공백 없는 문자열
    private final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    public boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return pattern.matcher(password).matches();
    }
    // 이메일 유효성 검사 정규식
    private final String EMAIL_PATTERN =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private final Pattern patternEmail = Pattern.compile(EMAIL_PATTERN);
    // 이메일 유효성 검사 메서드
    public boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return patternEmail.matcher(email).matches();
    }
}
