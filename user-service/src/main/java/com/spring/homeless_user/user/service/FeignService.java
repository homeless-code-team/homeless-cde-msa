package com.spring.homeless_user.user.service;

import com.spring.homeless_user.user.dto.FeignResDto;
import com.spring.homeless_user.user.dto.UserResponseDto;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class FeignService {

    private final UserRepository userRepository;

    public List<FeignResDto> existsByEmailAndRefreshToken(List<String> result) {
        List<FeignResDto> friendsList = new ArrayList<>(); // 최종 반환할 리스트

        try {
            for (String email : result) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("사용자가 없습니다.: " + email));

                FeignResDto friendsDto = new FeignResDto();
                friendsDto.setReceiverNickname(user.getNickname());
                friendsDto.setProfileImage(user.getProfileImage());

                friendsList.add(friendsDto);
            }

        } catch (Exception ignored) {
        }

        return friendsList;
    }

    public String changeEmail(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UsernameNotFoundException(nickname));
        return user.getEmail();
    }

    public List<UserResponseDto> findByEmailIn(List<String> userEmails) {

        List<User> byEmailIn = userRepository.findByEmailIn(userEmails);

        return byEmailIn.stream().map(user ->
                new UserResponseDto(user.getEmail(), user.getNickname(), user.getProfileImage())).collect(Collectors.toList());

    }


    public UserResponseDto findFriendByEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return new UserResponseDto(user.getEmail(), user.getNickname(), user.getProfileImage());
        }
        else {
            throw new UsernameNotFoundException("User not found by email: " + email);
        }
    }
}
