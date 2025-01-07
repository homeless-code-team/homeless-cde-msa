package com.spring.homeless_user.user.service;


import com.spring.homeless_user.common.auth.JwtTokenProvider;
import com.spring.homeless_user.common.utill.JwtUtil;
import com.spring.homeless_user.common.utill.SecurityContextUtil;
import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
//    private final AmazonS3 amazonS3;


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 주입할 RedisTemplate
    private final RedisTemplate<String, String> checkTemplate;
    private final RedisTemplate<String, String> loginTemplate;
    private final SecurityContextUtil securityContextUtil;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JavaMailSender mailSender,
                       JwtUtil jwtUtil,
                       @Qualifier("check") RedisTemplate<String, String> checkTemplate,
                       @Qualifier("login") RedisTemplate<String, String> loginTemplate,
                       SecurityContextUtil securityContextUtil
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
        this.checkTemplate = checkTemplate;
        this.loginTemplate = loginTemplate;
        this.securityContextUtil = securityContextUtil;
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    @Value("${jwt.expirationRt}")
    private long expirationTimeRt;

//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucketName;


//    public String uploadFile(MultipartFile file) throws IOException {
//        if (!file.isEmpty()) {
//            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
//            amazonS3.putObject(bucketName, fileName, file.getInputStream(), null);
//            return amazonS3.getUrl(bucketName, fileName).toString(); // 업로드된 파일의 URL 반환
//        } else {
//            return null;
//        }
//    }
/////////////////////////////////////////////////////// 사용자 인증 인가///////////////////////////////////////////////////

    //회원가입 로직
    public CommonResDto userSignUp(@Valid UserSaveReqDto dto) throws IOException {

        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sign-up", "/api/v1/users/sign-up", "POST"));
        links.add(new CommonResDto.Link("login", "/api/v1/users/sign-in", "POST"));
        links.add(new CommonResDto.Link("profileModify", "/api/v1/users", "PATCH"));
        links.add(new CommonResDto.Link("logout", "/api/v1/users/logout", "POST"));
        links.add(new CommonResDto.Link("Delete", "/api/v1/users", "DELETE"));


        try {
            // 비밀번호 정규성 검사
            if (!isValidPassword(dto.getPassword())){
                return new CommonResDto(HttpStatus.BAD_REQUEST,401,"비밀번호가 유효하지 않습니다.",null,links);
            }
            if(!isValidEmail(dto.getEmail())){
                return new CommonResDto(HttpStatus.BAD_REQUEST,401,"아메일이 유효하지 않습니다.",null,links);
            }
            // user객체 생성 및 dto 정보를 엔티티에 주입 
            User user = new User();
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setNickname(dto.getNickname());
            user.setCreatedAt(LocalDateTime.now());
//        user.setProfileImage(uploadFile(dto.getProfileImage()));

            log.info("email:{},nickname:{},password:{},CreatAT:{}", dto.getEmail(), dto.getNickname(), dto.getPassword(), user.getCreatedAt());

            // entity 객체를 mysql에 저장
            userRepository.save(user);

            // 응답 반환 
            return new CommonResDto(HttpStatus.OK, 201, "회원가입을 환영합니다.", null, links);

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
//        if (loginTemplate.opsForValue().get(dto.getEmail())!=null){
//            CommonResDto.Link Link = new CommonResDto.Link("login", "api/v1/users/sign-in","POST");
//            return new CommonResDto(HttpStatus.BAD_REQUEST, 401,"이미 로그인 중입니다.",null,List.of(Link));
//        }
        // mysql에서 사용자 검색
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + dto.getEmail()));
        log.info(user.toString());

        try {
            // 비밀번호 일치여부 확인
            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "Invalid password.", null, links);
            }

            // refreshToken 생성
            String refreshToken = jwtTokenProvider.refreshToken(dto.getEmail(), user.getId());

            // mysql에 refreshToken 저장
            user.setRefreshToken(refreshToken);
            log.info("refreshToken:{}", refreshToken);

            userRepository.save(user);
            log.info("user:{} 로그인 성공", user);

            // accesstoken 생성
            String accessToken = jwtTokenProvider.accessToken(dto.getEmail(), user.getId(), user.getNickname());

            // accessToken redis에 저장
            loginTemplate.opsForValue().set(dto.getEmail(), accessToken,30, TimeUnit.MINUTES);
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

        try {
            // 토큰을 통해서 저장한 이메일 불러오기 
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            //mysql에서 email을 기반으로 사용자 검색
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + email));

            //refreshToken mysql에서 지우기
            user.setRefreshToken(null);
            userRepository.save(user);
            log.info("userRefreshToken:{}", user.getRefreshToken());

            //레디스에서 이메일관련 accessToken제거
            loginTemplate.delete(email);

            return new CommonResDto(HttpStatus.OK, 200, "SignOut successfully.", null, List.of(Link));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "로그아웃중 에러 발생", e.getMessage(), List.of(Link));
        }

    }

    //토큰갱신
    public CommonResDto refreshToken(UserLoginReqDto dto) {

        // REST API 링크 설정
        CommonResDto.Link Link = new CommonResDto.Link("TokenRefresh", "api/v1/users/refresh", "POST");

        //토큰 유효성 검사
        try {
            // mysql에서 사용자검색
            User user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + dto.getEmail()));
            //userId 불러어기
            String userId = user.getId();
            // 리프레쉬 토큰 유효성 검사
            String refreshToken = user.getRefreshToken();
            boolean flag = jwtUtil.isTokenExpired(refreshToken);
            log.info(refreshToken);
            log.info(String.valueOf(flag));

            if (!flag) {
                //새로운 엑세스 토큰 생성
                String newAccessToken = jwtTokenProvider.accessToken(dto.getEmail(), userId, user.getNickname());
                log.info(newAccessToken);
                // 원래있던 access token 삭제
                loginTemplate.delete(dto.getEmail());
                // accesstoken 재발급 받은걸로 다시 저장
                loginTemplate.opsForValue().set(dto.getEmail(), newAccessToken);

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
    public CommonResDto confirm(String email, String token) {
        // REST API 링크 설정
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sendEmail", "/api/v1/users/confirm", "POST"));
        links.add(new CommonResDto.Link("checkEmail", "/api/v1/users/confrim", "GET"));
        try {

            //redis에 현재 토큰이 있는 지 확인
            String redisEmail = checkTemplate.opsForValue().get(token);

            // 비밀번호 찾기 이메일 인증
            if (!redisEmail.isEmpty() && userRepository.findByEmail(email).isPresent()) {
                if (redisEmail.equals(email)) {
                    checkTemplate.delete(token);
                    return new CommonResDto(HttpStatus.OK, 200, "token 유효, 비밀번호를 수정해주세요", null, links);
                }
                //회원가입 미메일 인증
            } else if (!redisEmail.isEmpty() && !userRepository.findByEmail(email).isPresent()) {
                if (redisEmail.equals(email)) {
                    checkTemplate.delete(token);
                    return new CommonResDto(HttpStatus.OK, 200, "token 유효, 회원가입을 계속 진행하세요", null, links);
                }
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "token 유효하지 않음, 재 인증해주세요", "null", links);
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
                boolean emailExists = userRepository.findByEmail(email).isPresent();
                return emailExists
                        ? new CommonResDto(HttpStatus.OK, 20, "이메일 사용 불가", null, links)
                        : new CommonResDto(HttpStatus.OK, 200, "이메일을 사용해도 좋아요.", null, links);
            } else if (nickname != null) {
                //닉네임 중복체크
                boolean nicknameExists = userRepository.findByNickname(nickname).isPresent();
                return nicknameExists
                        ? new CommonResDto(HttpStatus.OK, 200, "닉네임 사용 불가", null, links)
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
            // mysql에서 삭제
            userRepository.deleteByEmail(email);
            //redis에서 삭제
            loginTemplate.delete(email);

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
            String userId = SecurityContextUtil.getCurrentUser().getUserId();
            String email = securityContextUtil.getCurrentUser().getEmail();
            // 사용자 검색
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + email));

            // 닉네임 변경
            if (dto.getNickname() != null) {
                boolean nicknameExists = userRepository.findByNickname(dto.getNickname()).isPresent();
                if (nicknameExists) {
                    return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "닉네임이 이미 존재합니다.", null, links);
                }
                user.setNickname(dto.getNickname());
                userRepository.save(user);
                return new CommonResDto(HttpStatus.OK, 200, "닉네임변경성공", null, links);
            } else if (dto.getPassword() != null) {
                if (!isValidPassword(dto.getPassword())){
                    return new CommonResDto(HttpStatus.BAD_REQUEST,401,"비밀번호가 유효하지 않습니다.",null,links);
                }
                // 비밀번호 수정
                String hashedPassword = passwordEncoder.encode(dto.getPassword());
                user.setPassword(hashedPassword);
                userRepository.save(user);
                return new CommonResDto(HttpStatus.OK, 200, "페스워드변경성공", null, links);
            } else if (dto.getContent() != null) {
                // 소개글 수정
                user.setContents(dto.getContent());
                userRepository.save(user);
                return new CommonResDto(HttpStatus.OK, 200, "소개글변경성공", null, links);
            } else if (dto.getProfileImage() != null) {
                String oldProfileImage = user.getProfileImage(); // 기존 프로필 이미지 URL
                String bucketName = "your-s3-bucket-name";
                String newProfileImageUrl;

//                // S3에서 기존 이미지 삭제
//                if (oldProfileImage != null && !oldProfileImage.isEmpty()) {
//                    String oldKey = oldProfileImage.replace("https://your-s3-bucket-name.s3.amazonaws.com/", "");
//                    amazonS3.deleteObject(new DeleteObjectRequest(bucketName, oldKey));
//                }
//
//                // 새 이미지 업로드
//                newProfileImageUrl = uploadFile(bucketName, img);
//
//                user.setProfileImage(newProfileImageUrl);
//                // DB 업데이트
//                userRepository.save(user);
                return new CommonResDto(HttpStatus.OK, 200, "이미지변경성공", null, links);
            }
            return new CommonResDto(HttpStatus.BAD_REQUEST, 401, "사용자 수정 정보가 없습니다.", null, links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "에러 발생: " + e.getMessage(), null, links);
        }
    }

    // 회원정보조회
    public CommonResDto getUserData(){
         // REST API 링크 설정
            List<CommonResDto.Link> links = new ArrayList<>();
            links.add(new CommonResDto.Link("sign-in", "/api/v1/users/sign-in", "POST"));
            links.add(new CommonResDto.Link("modify", "/api/v1/users", "PATCH"));
        try{
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + email));
            GetUserDto dto = new GetUserDto();
            dto.setNickname(user.getNickname());
            dto.setEmail(user.getEmail());
            dto.setContent(user.getContents());
            dto.setProfileImage(user.getProfileImage());
            return new CommonResDto(HttpStatus.OK, 200, "조회성공", dto, links);
        }catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 400, "에러 발생: " + e.getMessage(), null, links);
        }
    }

    // 비밀번호 유효성 검사 정규식
    private static final String PASSWORD_PATTERN =
                    "^(?=.*[0-9])" +        // 적어도 1개의 숫자
                    "(?=.*[a-z])" +         // 적어도 1개의 소문자
                    "(?=.*[A-Z])" +         // 적어도 1개의 대문자
                    "(?=.*[!@#$%^&+=])" +    // 적어도 1개의 특수문자
                    "(?=\\S+$).{8,16}$";    // 8~16자의 공백 없는 문자열

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
}


