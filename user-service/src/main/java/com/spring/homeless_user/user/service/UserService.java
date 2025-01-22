package com.spring.homeless_user.user.service;

import com.spring.homeless_user.common.auth.JwtTokenProvider;
import com.spring.homeless_user.common.utill.JwtUtil;
import com.spring.homeless_user.common.utill.SecurityContextUtil;
import com.spring.homeless_user.user.Oauth.GoogleOAuthProperties;
import com.spring.homeless_user.user.config.S3Upload;
import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.entity.Provider;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;
    private final CacheManager cacheManager;
    private final WebClient webClient;


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 주입할 RedisTemplate
    private final RedisTemplate<String, String> checkTemplate;
    private final RedisTemplate<String, String> loginTemplate;
    private final RedisTemplate<String, String> cacheTemplate;
    private final SecurityContextUtil securityContextUtil;
    private final RedisTemplate login;
    private final S3Upload s3Upload;
    private final GoogleOAuthProperties googleOAuthProperties;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JavaMailSender mailSender,
                       JwtUtil jwtUtil,
                       CacheManager cacheManager, WebClient.Builder webClientBuilder,
                       @Qualifier("check") RedisTemplate<String, String> checkTemplate,
                       @Qualifier("login") RedisTemplate<String, String> loginTemplate,
                       @Qualifier("cache") RedisTemplate<String, String> cacheTemplate,
                       SecurityContextUtil securityContextUtil,
                       RefreshAutoConfiguration refreshAutoConfiguration,
                       @Qualifier("login") RedisTemplate login,
                       S3Upload s3Upload, GoogleOAuthProperties googleOAuthProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
        this.cacheManager = cacheManager;
        this.webClient = webClientBuilder.build();
        this.checkTemplate = checkTemplate;
        this.loginTemplate = loginTemplate;
        this.cacheTemplate = cacheTemplate;
        this.securityContextUtil = securityContextUtil;
        this.login = login;
        this.s3Upload = s3Upload;
        this.googleOAuthProperties = googleOAuthProperties;
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expirationRt}")
    private long expirationTimeRt;

    @Value("${jwt.expiration}")
    private long expirationTime;


    @Value("${oauth.provider.google.tokenUrl}")
    private String googleTokenUrl;

    @Value("${oauth.provider.google.userInfoUrl}")
    private String googleUserInfoUrl;

    @Value("${oauth.provider.google.clientId}")
    private String googleClientId;

    @Value("${oauth.provider.google.clientSecret}")
    private String googleClientSecret;

    @Value("${oauth.provider.google.redirectUri}")
    private String googleRedirectUri;

    @Value("${oauth.provider.github.tokenUrl}")
    private String githubTokenUrl;

    @Value("${oauth.provider.github.userInfoUrl}")
    private String githubUserInfoUrl;

    @Value("${oauth.provider.github.client-id}")
    private String githubClientId;

    @Value("${oauth.provider.github.client-secret}")
    private String githubClientSecret;

    @Value("${oauth.provider.github.redirect-uri}")
    private String githubRedirectUri;


/////////////////////////////////////////////////////// 사용자 인증 인가///////////////////////////////////////////////////

    //회원가입 로직
    public CommonResDto userSignUp(UserSaveReqDto dto) throws IOException {

        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sign-up", "/api/v1/users/sign-up", "POST"));
        links.add(new CommonResDto.Link("login", "/api/v1/users/sign-in", "POST"));
        links.add(new CommonResDto.Link("profileModify", "/api/v1/users", "PATCH"));
        links.add(new CommonResDto.Link("logout", "/api/v1/users/logout", "POST"));
        links.add(new CommonResDto.Link("Delete", "/api/v1/users", "DELETE"));


        try {
            log.info(dto.getEmail());
            log.info(dto.getPassword());
            log.info(dto.getNickname());
            // 비밀번호 정규성 검사
            if (!isValidPassword(dto.getPassword())) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "비밀번호가 유효하지 않습니다.", null, links);
            }
            if (!isValidEmail(dto.getEmail())) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "아메일이 유효하지 않습니다.", null, links);
            }
            log.info(dto.getEmail());
            log.info(dto.getPassword());
            log.info(dto.getNickname());
            // user객체 생성 및 dto 정보를 엔티티에 주입
            User user = new User();
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setNickname(dto.getNickname());
            user.setProvider(Provider.LOCAL);
            user.setCreatedAt(LocalDateTime.now());

            log.info("email:{},nickname:{},password:{},CreatAT:{}", dto.getEmail(), dto.getNickname(), dto.getPassword(), user.getCreatedAt());

            // entity 객체를 mysql에 저장
            userRepository.save(user);
            // 응답 반환
            return new CommonResDto(HttpStatus.OK, 200, "회원가입을 환영합니다.", null, links);

        } catch (Exception e) {
            //에러 응답 반환
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 401, "에러발생" + e.getMessage(), null, links);
        }
    }

    // 로그인로직
    public CommonResDto userSignIn(@Valid UserLoginReqDto dto) {

        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("login", "/api/v1/users/sign-in", "POST"));
        links.add(new CommonResDto.Link("profileModify", "/api/v1/users", "PATCH"));
        links.add(new CommonResDto.Link("logout", "/api/v1/users/logout", "POST"));
        links.add(new CommonResDto.Link("Delete", "/api/v1/users", "DELETE"));
        // 레디스에 이미 로그인 중인지 확인
//        if (loginTemplate.opsForValue().get(dto.getEmail()) != null) {
//            CommonResDto.Link Link = new CommonResDto.Link("login", "api/v1/users/sign-in", "POST");
//            return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "이미 로그인 중입니다.", null, List.of(Link));
//        }
        // mysql에서 사용자 검색


        try {
            String email = dto.getEmail();
            User user = getUserEntity(email);
            log.info(user.toString());
            // 비밀번호 일치여부 확인
            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "Invalid password.", null, links);
            }

            // refreshToken 생성
            String refreshToken = jwtTokenProvider.refreshToken(email, user.getId());

            // mysql에 refreshToken 저장
            user.setRefreshToken(refreshToken);
            log.info("refreshToken:{}", refreshToken);

            userRepository.save(user);
            log.info("user:{} 로그인 성공", user);

            // accestoken 생성
            String accessToken = jwtTokenProvider.accessToken(email, user.getId(), user.getNickname());

            // accessToken redis에 저장
            loginTemplate.opsForValue().set(dto.getEmail(), accessToken, 30, TimeUnit.MINUTES);
            log.info("accessToken:{}", accessToken);


            return new CommonResDto(HttpStatus.OK, 200, "SignIn successfully.", accessToken, links);
        } catch (Exception e) {
            e.printStackTrace();
            CommonResDto.Link Link = new CommonResDto.Link("login", "api/v1/users/sign-in", "POST");
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 402, "에러발생" + e.getMessage(), null, List.of(Link));
        }
    }

    //로그아웃
    public CommonResDto userSignOut() {
        // REST API 링크 설정
        CommonResDto.Link Link = new CommonResDto.Link("logout", "api/v1/users/sign-out", "DELETE");
        // 토큰을 통해서 저장한 이메일 불러오기

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            //mysql에서 email을 기반으로 사용자 검색
            User user = getUserEntity(email);

            //refreshToken mysql에서 지우기
            user.setRefreshToken(null);
            userRepository.save(user);
            log.info("userRefreshToken:{}", user.getRefreshToken());

            //레디스에서 이메일관련 accessToken제거
            loginTemplate.delete(email);

            //레디스 캐싱 내용 지우기
            cacheTemplate.delete(email);

            return new CommonResDto(HttpStatus.OK, 200, "SignOut successfully.", null, List.of(Link));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "로그아웃중 에러 발생", e.getMessage(), List.of(Link));
        }

    }

    //토큰갱신
    public CommonResDto refreshToken(String id) {

        // REST API 링크 설정
        CommonResDto.Link Link = new CommonResDto.Link("TokenRefresh", "api/v1/users/refresh", "POST");

        //토큰 유효성 검사
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + id));
            // 리프레쉬 토큰 유효성 검사
            String refreshToken = user.getRefreshToken();
            boolean flag = jwtUtil.isTokenExpired(refreshToken);
            log.info(refreshToken);
            log.info(String.valueOf(flag));

            if (!flag) {
                //새로운 엑세스 토큰 생성
                String newAccessToken = jwtTokenProvider.accessToken(user.getEmail(), id, user.getNickname());
                log.info(newAccessToken);
                // 원래있던 access token 삭제
                loginTemplate.delete(user.getEmail());
                // accesstoken 재발급 받은걸로 다시 저장
                loginTemplate.opsForValue().set(user.getEmail(), newAccessToken);

                return new CommonResDto(HttpStatus.OK, 200, "Refresh token successfully.", newAccessToken, List.of(Link));
            } else {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "Invalid refresh token.", null, List.of(Link));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "이메일 access token error", null, List.of(Link));
        }
    }

    // 인증 이메일 전송 로직 인증번호 10분 유효 (이메일 만 필요)
    public CommonResDto sendVerificationEmail(EmailCheckDto dto) {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sendEmail", "/api/v1/users/confirm", "POST"));
        links.add(new CommonResDto.Link("checkEmail", "/api/v1/users/confrim", "GET"));

        log.info(dto.toString());

        //  이메일 인증 임시토큰 발급
        String token = jwtTokenProvider.emailToken(dto.getEmail());

        // 이메일 제목과 본문 구성
        String subject = "이메일 인증을 해주세요";

        // 이메일 본문: 인증번호 포함
        String text = "<p>이메일 인증을 완료하려면 아래 인증번호를 입력창에 입력해주세요:</p>" +
                "<h3>" + token + "</h3>";

        try {
            // MimeMessage 생성
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 이메일 정보 설정
            helper.setTo(dto.getEmail());
            helper.setSubject(subject);
            helper.setText(text, true); // HTML 형식으로 전송

            // 이메일 전송
            mailSender.send(message);

            // key-value와 TTL 설정 저장 10분-> 10분이후에 redis에서 자동삭제
            checkTemplate.opsForValue().set(token, dto.getEmail(), Duration.ofMinutes(10));


            return new CommonResDto(HttpStatus.OK, 200, "이메일 전송 성공!!!", token, links);
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 401, "이메일 전송 실패", null, links);
        }
    }

    // 이메일 인증, 비밀번호 회원가입시
    public CommonResDto confirm(EmailCheckDto dto) {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sendEmail", "/api/v1/users/confirm", "POST"));
        links.add(new CommonResDto.Link("checkEmail", "/api/v1/users/confrim", "GET"));
        try {

            String token = dto.getToken();
            String email = dto.getEmail();
            log.info(token);
            log.info(email);
            //redis에 현재 토큰이 있는 지 확인
            String redisEmail = checkTemplate.opsForValue().get(token);
            boolean equals = Boolean.TRUE.equals(checkTemplate.hasKey(token));
            log.info(redisEmail);
            boolean flag = userRepository.findByEmail(email).isPresent();
            log.info(String.valueOf(flag));
            // 비밀번호 찾기 이메일 인증
            if (equals) {
                if (flag) {
                    //비밀번호 수정
                    checkTemplate.delete(token);
                    return new CommonResDto(HttpStatus.OK, 200, "token 유효, 비밀번호를 수정해주세요", null, links);
                } else {
                    // 회원가입진행
                    checkTemplate.delete(token);
                    return new CommonResDto(HttpStatus.OK, 200, "token 유효, 회원가입을 계속 진행하세요", null, links);
                }
            }

            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "토큰이 저장되지 않았습니다", null, links);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "에러발생" + e.getMessage(), null, links);
        }
    }

    // 이메일 &닉네임 중복검사
    public CommonResDto duplicateCheck(String email, String nickname) {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("duplicate", "/api/v1/users/duplicate", "GET"));
        links.add(new CommonResDto.Link("sign-up", "/api/v1/users/sign-up", "POST"));
        links.add(new CommonResDto.Link("modify", "/api/v1/users", "PATCH"));
        try {
            // 이메일 중복 체크
            if (email != null) {
                boolean exists = userRepository.findByEmail(email).isPresent();
                return exists
                        ? new CommonResDto(HttpStatus.BAD_REQUEST, 401, "이메일 사용 불가", null, links)
                        : new CommonResDto(HttpStatus.OK, 200, "이메일을 사용해도 좋아요.", null, links);
            } else if (nickname != null) {
                //닉네임 중복체크
                boolean nicknameExists = userRepository.findByNickname(nickname).isPresent();
                return nicknameExists
                        ? new CommonResDto(HttpStatus.BAD_REQUEST, 400, "닉네임 사용 불가", null, links)
                        : new CommonResDto(HttpStatus.OK, 200, "닉네임을 사용해도 좋아요.", null, links);
            }

            // 이메일과 닉네임 모두 null인 경우
            return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "잘못된 요청입니다.", null, links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 401, "에러 발생", e.getMessage(), links);
        }
    }

    //회원탈퇴
    public CommonResDto delete() {
        // REST API 링크 설정
        CommonResDto.Link Link = new CommonResDto.Link("Delete", "api/v1/users", "DELETE");
        try {
            // 이메일 불러오기 
            String email = securityContextUtil.getCurrentUser().getEmail();
            User user = getUserEntity(email);
            // mysql에서 삭제
            userRepository.delete(user);

            //redis에서 삭제
            loginTemplate.delete(email);

            //캐싱 삭제
            cacheTemplate.delete(email);

            return new CommonResDto(HttpStatus.OK, 200, "삭제완료", null, List.of(Link));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "에러발생" + e.getMessage(), null, List.of(Link));
        }
    }

    // 회원정보수정
    public CommonResDto modify(ModifyDto dto) {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sign-in", "/api/v1/users/sign-in", "POST"));
        links.add(new CommonResDto.Link("modify", "/api/v1/users", "PATCH"));

        try {
            // 현재 인증된 사용자 가져오기
            String email = securityContextUtil.getCurrentUser().getEmail();
            // 사용자 검색
            User user = getUserEntity(email);

            log.info(String.valueOf(user));

            // 닉네임 변경
            if (dto.getNickname() != null) {
                boolean nicknameExists = userRepository.findByNickname(dto.getNickname()).isPresent();
                log.info(String.valueOf(nicknameExists));
                if (nicknameExists) {
                    return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "닉네임이 이미 존재합니다.", null, links);
                }
                user.setNickname(dto.getNickname());
                updateUserEntity(email, user);
                return new CommonResDto(HttpStatus.OK, 200, "닉네임변경성공", user.getNickname(), links);
            } else if (dto.getPassword() != null) {
                if (!isValidPassword(dto.getPassword())) {
                    return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "비밀번호가 유효하지 않습니다.", null, links);
                }
                // 비밀번호 수정
                String hashedPassword = passwordEncoder.encode(dto.getPassword());
                user.setPassword(hashedPassword);
                updateUserEntity(email, user);
                return new CommonResDto(HttpStatus.OK, 200, "페스워드변경성공", user.getPassword(), links);
            } else if (dto.getContent() != null) {
                user.setContents(dto.getContent());
                updateUserEntity(email, user);
                return new CommonResDto(HttpStatus.OK, 200, "소개글변경성공", user.getContents(), links);
            } else if (dto.getProfileImage() != null) {
                String profileImageUrl = s3Upload.uploadFile(dto.getProfileImage());
                user.setProfileImage(profileImageUrl);
                log.info("Profile Image Uploaded: {}", profileImageUrl);
                updateUserEntity(email, user);
                return new CommonResDto(HttpStatus.OK, 200, "이미지변경성공", user.getProfileImage(), links);
            }


            return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "사용자 수정 정보가 없습니다.", null, links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "에러 발생: " + e.getMessage(), null, links);
        }
    }

    // 회원정보조회
    public CommonResDto getUserData() {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sign-in", "/api/v1/users/sign-in", "POST"));
        links.add(new CommonResDto.Link("modify", "/api/v1/users", "PATCH"));
        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            User user = getUserEntity(email);
            GetUserDto dto = new GetUserDto();
            dto.setNickname(user.getNickname());
            dto.setEmail(user.getEmail());
            dto.setContent(user.getContents());
            dto.setProfileImage(user.getProfileImage());
            return new CommonResDto(HttpStatus.OK, 200, "조회성공", dto, links);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "에러 발생: " + e.getMessage(), null, links);
        }
    }

    // 사용자 검색


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 비밀번호 유효성 검사 정규식
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])" +                 // 적어도 1개의 숫자
                    "(?=.*[a-z])" +                  // 적어도 1개의 소문자
                    "(?=.*[A-Z])" +                  // 적어도 1개의 대문자
                    "(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~])" + // 적어도 1개의 특수문자
                    "(?=\\S+$).{8,16}$";             // 8~16자의 공백 없는 문자열

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return pattern.matcher(password).matches();
    }

    // 이메일 유효성 검사 정규식
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private static final Pattern patternEmail = Pattern.compile(EMAIL_PATTERN);

    // 이메일 유효성 검사 메서드
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return patternEmail.matcher(email).matches();
    }

    //////////////////////////////////////////////////Cahe//////////////////////////////////////////////////////////////////
    //캐싱 메서드
    @Cacheable(value = "userCache", key = "#email")
    public User getUserEntity(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    // 캐싱 수정
    @CachePut(value = "userCache", key = "#email")
    public User updateUserEntity(String email, User updatedUser) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        // 엔티티 수정
        existingUser.setNickname(updatedUser.getNickname());
        existingUser.setContents(updatedUser.getContents());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setProfileImage(updatedUser.getProfileImage());
        userRepository.save(existingUser);

        return existingUser; // 캐시에 저장됨
    }

    public CommonResDto alluser() {
        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            // User 엔티티 리스트를 가져옴
            List<User> users = userRepository.findAll();
            // User 엔티티 리스트를 가져오고, 현재 사용자 이메일은 제외
            List<User> users1 = userRepository.findAll().stream()
                    .filter(user -> !user.getEmail().equals(email)) // 현재 유저 제외
                    .toList();
            // DTO 리스트로 변환
            List<AllDto> allDtos = users1.stream()
                    .map(user -> new AllDto(user.getNickname(), user.getProfileImage())) // 필요한 필드만 DTO에 매핑
                    .collect(Collectors.toList());

            // CommonResDto에 담아 반환
            return new CommonResDto(HttpStatus.OK, 200, "유저 리스트 조회 성공", allDtos, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public CommonResDto CheckAccessToken(String token) {

        Claims claims = jwtUtil.extractAllClaims(token);
        String email = claims.getSubject();
        String accesstoken = loginTemplate.opsForValue().get(email);
        if (accesstoken == null) {
            return  new CommonResDto(HttpStatus.BAD_REQUEST,401,"만료된 엑세스토큰",null,null);
        }
        return new CommonResDto(HttpStatus.OK,200,"토큰유효 사용가능",null,null);
    }




    ///////////////////////////////////////////////////OAuth///////////////////////////////////////////////////////////////////////
    // 프론트단에서 처음으로 소셜 로그인 버튼 누르면 받는 로직
    // 1. Access Token 요청
    public AccessTokenResponse getAccessTokenSync(String code) {
        log.info(code);
        try {
            // 요청 파라미터 설정
            return webClient.post()
                    .uri(googleOAuthProperties.getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("code", code)
                            .with("user_info", googleOAuthProperties.getUserInfoUrl())
                            .with("token_uri ", googleOAuthProperties.getTokenUrl())
                            .with("client_id", googleOAuthProperties.getClientId())
                            .with("client_secret", googleOAuthProperties.getClientSecret())
                            .with("redirect_uri", googleOAuthProperties.getRedirectUri())
                            .with("grant_type", "authorization_code"))
                    .retrieve()
                    .bodyToMono(AccessTokenResponse.class)
                    .block(); // 동기 방식으로 변경
        } catch (Exception e) {
            log.error("Failed to retrieve access token: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve access token", e);
        }
    }

    // 2. 사용자 정보 가져오기
    public GoogleUserInfo getUserInfoSync(String accessToken) {
        if (googleOAuthProperties.getUserInfoUrl() == null || googleOAuthProperties.getUserInfoUrl().isEmpty()) {
            throw new IllegalStateException("Google UserInfo URL is not configured");
        }

        log.info("Fetching user info from URL: {}", googleOAuthProperties.getUserInfoUrl());

        try {
            return webClient.get()
                    .uri(googleOAuthProperties.getUserInfoUrl())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(GoogleUserInfo.class)
                    .block(); // 동기 방식으로 변경
        } catch (Exception e) {
            log.error("Failed to fetch user info: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch user info", e);
        }
    }

    // 3. 회원가입 및 로그인 처리
    public CommonResDto processOAuthUserSync(GoogleUserInfo userInfo) {
        List<CommonResDto.Link> links = List.of(
                new CommonResDto.Link("sign-up", "/api/v1/users/sign-up", "POST"),
                new CommonResDto.Link("login", "/api/v1/users/sign-in", "POST")
        );
        log.info(userInfo.toString());
        log.info(userInfo.getEmail());
        log.info(userInfo.getName());

        try {
            Optional<User> optionalUser = userRepository.findByEmail(userInfo.getEmail());
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                log.info("조회된 user: {}", user);
                return handleLoginSync(user, links);
            } else {
                log.info("user is null. call handleSignUp");
                return handleSignUpSync(userInfo, links);
            }
        } catch (Exception e) {
            log.error("OAuth user processing failed: {}", e.getMessage());
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "에러 발생: " + e.getMessage(), null, links);
        }
    }

    private CommonResDto handleLoginSync(User existingUser, List<CommonResDto.Link> links) {
        String accessToken = jwtTokenProvider.accessToken(existingUser.getEmail(), existingUser.getId(), existingUser.getNickname());
        String refreshToken = jwtTokenProvider.refreshToken(existingUser.getEmail(), existingUser.getId());
        log.info(accessToken);
        log.info(refreshToken);
        loginTemplate.opsForValue().set(existingUser.getEmail(), accessToken);
        existingUser.setRefreshToken(refreshToken);
        userRepository.save(existingUser);

        return new CommonResDto(HttpStatus.OK, 200, "로그인 성공", accessToken, links);
    }

    private CommonResDto handleSignUpSync(GoogleUserInfo userInfo, List<CommonResDto.Link> links) {

        String profilImage = userInfo.getPicture();
        String nickName = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();

        String refreshToken = jwtTokenProvider.refreshToken(userInfo.getEmail(), id);

        User newUser = new User();
        newUser.setId(id);
        newUser.setEmail(userInfo.getEmail());
        newUser.setProfileImage(profilImage);
        newUser.setProvider(Provider.GOOGLE);
        newUser.setNickname(nickName);
        newUser.setRefreshToken(refreshToken);
        newUser.setNickname(nickName);
        newUser.setId(id);
        userRepository.save(newUser);

        String accessToken = jwtTokenProvider.accessToken(newUser.getEmail(), newUser.getId(), newUser.getNickname());
        loginTemplate.opsForValue().set(newUser.getEmail(), accessToken);

        return new CommonResDto(HttpStatus.CREATED, 201, "회원가입 성공", accessToken, links);
    }

    ////////////////////////////////////////////////////////feign 통신///////////////////////////////////////////////////////
    public List<FeignResDto> existsByEmailAndRefreshToken(List<String> result) {
        List<FeignResDto> friendsList = new ArrayList<>(); // 최종 반환할 리스트

        try {
            for (String email : result) { // 각 이메일에 대해 반복 처리
                // 이메일로 사용자 정보 조회
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

                // FriendsDto 객체 생성 및 데이터 설정
                FeignResDto friendsDto = new FeignResDto();
                friendsDto.setReceiverNickname(user.getNickname()); // 닉네임 설정
                friendsDto.setProfileImage(user.getProfileImage()); // RefreshToken 상태 설정

                // 리스트에 추가
                friendsList.add(friendsDto);
            }

        } catch (Exception e) {
            e.printStackTrace(); // 에러 로그 출력
        }

        return friendsList; // 최종 결과 반환
    }

    public String changeEmail(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UsernameNotFoundException(nickname));
        return user.getEmail();
}

    public List<UserResponseDto> findByEmailIn(List<String> userEmails) {

        List<User> byEmailIn = userRepository.findByEmailIn(userEmails);

        log.info("유저리스트 {}", byEmailIn);

        List<UserResponseDto> collect = byEmailIn.stream().map(user ->
                new UserResponseDto(user.getId(), user.getNickname(), user.getProfileImage())).collect(Collectors.toList());


        return collect;

    }


}


