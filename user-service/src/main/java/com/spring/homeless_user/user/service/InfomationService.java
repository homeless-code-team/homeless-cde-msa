package com.spring.homeless_user.user.service;

import com.spring.homeless_user.common.utill.SecurityContextUtil;
import com.spring.homeless_user.user.component.CacheComponent;
import com.spring.homeless_user.user.config.S3Upload;
import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.spring.homeless_user.user.utillity.CheckingUtil.isValidPassword;

@AllArgsConstructor
@Service
public class InfomationService {

    private final UserRepository userRepository;
    private final CacheComponent cacheComponent;
    private final PasswordEncoder passwordEncoder;
    private final S3Upload s3Upload;


    public CommonResDto getData(String nickname) {
        User user = userRepository.findByNickname(nickname).orElseThrow(() -> new UsernameNotFoundException(nickname));

        UserDataDto build = UserDataDto.builder()
                .contents(user.getContents())
                .profileImage(user.getProfileImage())
                .nickname(user.getNickname())
                .build();
        return new CommonResDto(HttpStatus.OK,200,"조회완료",build,null);

    }

    // 회원정보수정
    public CommonResDto modify(ModifyDto dto) {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sign-in", "/api/v1/users/sign-in", "POST"));
        links.add(new CommonResDto.Link("modify", "/api/v1/users", "PATCH"));

        try {
            // 현재 인증된 사용자 가져오기
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            // 사용자 검색
            User user = cacheComponent.getUserEntity(email);

            // 닉네임 변경
            if (dto.getNickname() != null) {
                boolean nicknameExists = userRepository.findByNickname(dto.getNickname()).isPresent();
                if (nicknameExists) {
                    return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "닉네임이 이미 존재합니다.", null, links);
                }
                user.setNickname(dto.getNickname());
                cacheComponent.updateUserEntity(email, user);
                return new CommonResDto(HttpStatus.OK, 200, "닉네임변경성공", user.getNickname(), links);
            } else if (dto.getPassword() != null) {
                if (!isValidPassword(dto.getPassword())) {
                    return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "비밀번호가 유효하지 않습니다.", null, links);
                }
                String hashedPassword = passwordEncoder.encode(dto.getPassword());
                user.setPassword(hashedPassword);
                cacheComponent.updateUserEntity(email, user);
                return new CommonResDto(HttpStatus.OK, 200, "페스워드변경성공", user.getPassword(), links);
            } else if (dto.getContent() != null) {
                user.setContents(dto.getContent());
                cacheComponent.updateUserEntity(email, user);
                return new CommonResDto(HttpStatus.OK, 200, "소개글변경성공", user.getContents(), links);
            } else if (dto.getProfileImage() != null) {
                String profileImageUrl = s3Upload.uploadFile(dto.getProfileImage());
                user.setProfileImage(profileImageUrl);
                cacheComponent.updateUserEntity(email, user);
                return new CommonResDto(HttpStatus.OK, 200, "이미지변경성공", user.getProfileImage(), links);
            }


            return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "사용자 수정 정보가 없습니다.", null, links);

        } catch (Exception e) {
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "에러 발생: " + e.getMessage(), null, links);
        }
    }

    public CommonResDto getUserData() {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sign-in", "/api/v1/users/sign-in", "POST"));
        links.add(new CommonResDto.Link("modify", "/api/v1/users", "PATCH"));

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            User user = cacheComponent.getUserEntity(email);

            GetUserDto dto = new GetUserDto();
            dto.setNickname(user.getNickname());
            dto.setEmail(user.getEmail());
            dto.setContent(user.getContents());
            dto.setProfileImage(user.getProfileImage());

            return new CommonResDto(HttpStatus.OK, 200, "조회성공", dto, links);
        } catch (Exception e) {
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "에러 발생: " + e.getMessage(), null, links);
        }
    }

    public CommonResDto alluser() {
        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            List<User> users1 = userRepository.findAll().stream()
                    .filter(user -> !user.getEmail().equals(email)) // 현재 유저 제외
                    .toList();

            List<AllDto> allDtos = users1.stream()
                    .map(user -> new AllDto(user.getNickname(), user.getProfileImage())) // 필요한 필드만 DTO에 매핑
                    .collect(Collectors.toList());
            return new CommonResDto(HttpStatus.OK, 200, "유저 리스트 조회 성공", allDtos, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
