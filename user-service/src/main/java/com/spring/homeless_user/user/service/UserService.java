package com.spring.homeless_user.user.service;


import com.spring.homeless_user.common.auth.JwtTokenProvider;
import com.spring.homeless_user.common.utill.JwtUtil;
import com.spring.homeless_user.common.utill.SecurityContextUtil;
import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.entity.AddStatus;
import com.spring.homeless_user.user.entity.Friends;
import com.spring.homeless_user.user.entity.Servers;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.FriendsRepository;
import com.spring.homeless_user.user.repository.ServerRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final FriendsRepository friendsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;
//    private final AmazonS3 amazonS3;

    // 주입할 RedisTemplate
    private final RedisTemplate<String, String> checkTemplate;
    private final RedisTemplate<String, String> loginTemplate;
    private final RedisTemplate<String, String> friendsTemplate;
    private final RedisTemplate<String, String> serverTemplate;
    private final SecurityContextUtil securityContextUtil;

    public UserService(UserRepository userRepository,
                       ServerRepository serverRepository,
                       FriendsRepository friendsRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JavaMailSender mailSender,
                       JwtUtil jwtUtil,
                       @Qualifier("check") RedisTemplate<String, String> checkTemplate,
                       @Qualifier("login") RedisTemplate<String, String> loginTemplate,
                       @Qualifier("friends") RedisTemplate<String, String> friendsTemplate,
                       @Qualifier("server") RedisTemplate<String, String> serverTemplate, SecurityContextUtil securityContextUtil
    ) {
        this.userRepository = userRepository;
        this.serverRepository = serverRepository;
        this.friendsRepository = friendsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
        this.checkTemplate = checkTemplate;
        this.loginTemplate = loginTemplate;
        this.friendsTemplate = friendsTemplate;
        this.serverTemplate = serverTemplate;
        this.securityContextUtil = securityContextUtil;
    }
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

    //회원가입 로직
    public CommonResDto userSignUp(@Valid UserSaveReqDto dto, MultipartFile img) throws IOException {
        try{
            if (dto.getEmail()==null || dto.getNickname()==null || dto.getPassword()==null){
                CommonResDto.Link loginLink = new CommonResDto.Link("login", "api/v1/users/sign-in","POST");
                return new CommonResDto(HttpStatus.BAD_REQUEST,400,"잘못된요청입니다.",null,List.of(loginLink));
            }
            User user = new User();
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setNickname(dto.getNickname());
            user.setCreatedAt(LocalDateTime.now());
//        user.setProfileImage(uploadFile(img));


            userRepository.save(user);

            List<CommonResDto.Link> links = new ArrayList<>();
            links.add(new CommonResDto.Link("login", "/api/v1/users/sign-in", "POST"));
            links.add(new CommonResDto.Link("profileModify", "/api/v1/users", "PATCH"));
            links.add(new CommonResDto.Link("logout", "/api/v1/users/logout", "POST"));
            links.add(new CommonResDto.Link("Delete", "/api/v1/users", "DELETE"));

            return new CommonResDto(HttpStatus.OK,201, "회원가입을 환영합니다.", null, links);
        }catch (Exception e){
            e.printStackTrace();
            List<CommonResDto.Link> links = new ArrayList<>();
            links.add(new CommonResDto.Link("sign-up", "/api/v1/users/sign-up", "POST"));
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,401,"에러발생"+e.getMessage(),null,links);
        }
    }

    // 로그인로직
    public CommonResDto userSignIn(UserLoginReqDto dto) {

//        if (loginTemplate.opsForValue().get(dto.getEmail())!=null){
//            CommonResDto.Link Link = new CommonResDto.Link("login", "api/v1/users/sign-in","POST");
//            return new CommonResDto(HttpStatus.BAD_REQUEST, 401,"이미 로그인 중입니다.",null,List.of(Link));
//        }
        if(dto.getEmail()==null||dto.getPassword()==null){
            CommonResDto.Link Link = new CommonResDto.Link("login", "api/v1/users/sign-in","POST");
            return new CommonResDto(HttpStatus.BAD_REQUEST,400,"잘못된 요청입니다.",null,List.of(Link));
        }
        User user =userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + dto.getEmail()));
        log.info(user.toString());
        try{
            CommonResDto.Link Link = new CommonResDto.Link("login", "api/v1/users/sign-in","POST");
            if (user == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST,400, "Invalid email.", null,List.of(Link));
            }
            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                return new CommonResDto(HttpStatus.BAD_REQUEST,400, "Invalid password.", null,List.of(Link));
            }
            //mysql에 refresh토큰 저장
            String refreshToken = jwtTokenProvider.refreshToken(dto.getEmail(),user.getId());
            user.setRefreshToken(refreshToken);

            userRepository.save(user);

            //redis에 accesstoken 저장
            String accessToken = jwtTokenProvider.accessToken(dto.getEmail(), user.getId(),user.getNickname());
            loginTemplate.opsForValue().set(dto.getEmail(), accessToken);

            List<CommonResDto.Link> links = new ArrayList<>();
            links.add(new CommonResDto.Link("login", "/api/v1/users/sign-in", "POST"));
            links.add(new CommonResDto.Link("profileModify", "/api/v1/users", "PATCH"));
            links.add(new CommonResDto.Link("logout", "/api/v1/users/logout", "POST"));
            links.add(new CommonResDto.Link("Delete", "/api/v1/users", "DELETE"));

            return new CommonResDto(HttpStatus.OK,200, "SignIn successfully.", accessToken,links);
        } catch (Exception e){
            e.printStackTrace();
            CommonResDto.Link Link = new CommonResDto.Link("login", "api/v1/users/sign-in","POST");
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,402,"에러발생"+e.getMessage(),null,List.of(Link));
        }
    }

    //로그아웃
    public CommonResDto userSignOut() {
        try{
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + email));

            //refreshToken 초기화
            user.setRefreshToken(null);
            userRepository.save(user);

            //레디스에서 이메일관련 accessToken제거
            loginTemplate.delete(email);
            CommonResDto.Link Link = new CommonResDto.Link("logout", "api/v1/users/sign-out","DELETE");
            return new CommonResDto(HttpStatus.OK,200, "SignOut successfully.", null,List.of(Link));
        }catch (Exception e){
            e.printStackTrace();
            CommonResDto.Link Link = new CommonResDto.Link("logout", "api/v1/users/sign-out","DELETE");
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,400,"로그아웃중 에러 발생",e.getMessage(),List.of(Link));
        }

    }

    //토큰갱신
    public CommonResDto refreshToken(UserLoginReqDto dto) {
        //토큰 유효성 검사
        try{
                User user = userRepository.findByEmail(dto.getEmail())
                        .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + dto.getEmail()));
                Long userId = user.getId();
            // 리프레쉬 토큰 유효성 검사
            String refreshToken = user.getRefreshToken();
            boolean flag = jwtUtil.isTokenExpired(refreshToken);
            log.info(refreshToken);
            log.info(String.valueOf(flag));
            if (!flag) {
                String newAccessToken = jwtTokenProvider.accessToken(dto.getEmail(), userId,user.getNickname());
                log.info(newAccessToken);
                loginTemplate.delete(dto.getEmail());
                loginTemplate.opsForValue().set(dto.getEmail(), newAccessToken);
                CommonResDto.Link Link = new CommonResDto.Link("TokenRefresh", "api/v1/users/refresh","POST");
                return new CommonResDto(HttpStatus.OK,200, "Refresh token successfully.", newAccessToken,List.of(Link));
            }else{
                CommonResDto.Link Link = new CommonResDto.Link("TokenRefresh", "api/v1/users/refresh","POST");
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400,"Invalid refresh token.", null,List.of(Link));
            }
        }catch(Exception e){
            e.printStackTrace();
            CommonResDto.Link Link = new CommonResDto.Link("TokenRefresh", "api/v1/users/refresh","POST");
                 return new CommonResDto(HttpStatus.BAD_REQUEST,400,"이메일 access token error",null,List.of(Link));
        }
    }

    // 인증 이메일 전송 로직 인증번호 10분 유효 (이메일 만 필요)
    public CommonResDto sendVerificationEmail(EmailCheckDto dto) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sendEmail", "/api/v1/users/confirm", "POST"));
        links.add(new CommonResDto.Link("checkEmail", "/api/v1/users/confrim", "GET"));
        if(dto.getEmail()==null){

            return new CommonResDto(HttpStatus.BAD_REQUEST,401,"잚못된 요청입니다.",null,links);
        }

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
            // key-value와 TTL 설정 저장 10분
            checkTemplate.opsForValue().set(token, dto.getEmail(), Duration.ofMinutes(10));


            return new CommonResDto(HttpStatus.OK,200,"이메일 전송 성공!!!", token,links);
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,401,"이메일 전송 실패",null,links);
        }
    }

    // 이메일 인증, 비밀번호 회원가입시
    public CommonResDto confirm(String email, String token) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sendEmail", "/api/v1/users/confirm", "POST"));
        links.add(new CommonResDto.Link("checkEmail", "/api/v1/users/confrim", "GET"));
       try{
           if (email== null && token==null){
               return new CommonResDto(HttpStatus.BAD_REQUEST,400,"잘못된 요청입니다.",null,links);
           }
           String redisEmail = checkTemplate.opsForValue().get(token);

           // 비밀번호 찾기 이메일 인증
           if(!redisEmail.isEmpty() && userRepository.findByEmail(email).isPresent()) {
                if (redisEmail.equals(email)) {
                    checkTemplate.delete(token);
                    return new CommonResDto(HttpStatus.OK,200, "token 유효, 비밀번호를 수정해주세요", null,links);
                }
           //회원가입 미메일 인증     
            }else if(!redisEmail.isEmpty()&&!userRepository.findByEmail(email).isPresent()) {
               if (redisEmail.equals(email)) {
                   checkTemplate.delete(token);
                   return new CommonResDto(HttpStatus.OK,200, "token 유효, 회원가입을 계속 진행하세요",null,links);
               }
               return new CommonResDto(HttpStatus.BAD_REQUEST,400, "token 유효하지 않음, 재 인증해주세요", "null",links);
            }
           return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,400,"토큰이 저장되지 않았습니다",null,links);
        }catch (Exception e){
           e.printStackTrace();
           return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,400,"에러발생"+e.getMessage(),null,links);
       }
    }

    // 이메일 &닉네임 중복검사
    public CommonResDto duplicateCheck(String email, String nickname) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("duplicate", "/api/v1/users/duplicate", "GET"));
        links.add(new CommonResDto.Link("sign-up", "/api/v1/users/sign-up", "POST"));
        links.add(new CommonResDto.Link("modify","/api/v1/users","PATCH"));
        try {
            // 이메일 중복 체크
            if (email != null) {
                boolean emailExists = userRepository.findByEmail(email).isPresent();
                return emailExists
                        ? new CommonResDto(HttpStatus.OK,20, "이메일 사용 불가", null,links)
                        : new CommonResDto(HttpStatus.OK,200, "이메일을 사용해도 좋아요.", null,links);
            }

            // 닉네임 중복 체크
            if (nickname != null) {
                boolean nicknameExists = userRepository.findByNickname(nickname).isPresent();
                return nicknameExists
                        ? new CommonResDto(HttpStatus.OK,200, "닉네임 사용 불가", null,links)
                        : new CommonResDto(HttpStatus.OK,200, "닉네임을 사용해도 좋아요.", null,links);
            }

            // 이메일과 닉네임 모두 null인 경우
            return new CommonResDto(HttpStatus.BAD_REQUEST,400, "잘못된 요청입니다.", null,links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,401, "에러 발생", e.getMessage(),links );
        }
    }

    //회원탈퇴
    public CommonResDto delete() {
        CommonResDto.Link Link = new CommonResDto.Link("Delete", "api/v1/users","DELETE");
        try{
            String email = securityContextUtil.getCurrentUser().getEmail();
            userRepository.deleteByEmail(email);
            loginTemplate.delete(email);

            return new CommonResDto(HttpStatus.OK,200, "삭제완료", null,List.of(Link));
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,400, "에러발생"+e.getMessage(),null,List.of(Link));
        }
    }

// 회원정보수정
    public CommonResDto modify(ModifyDto dto, MultipartFile img) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("sign-in", "/api/v1/users/sign-in", "POST"));
        links.add(new CommonResDto.Link("modify","/api/v1/users","PATCH"));

        try {
            // 현재 인증된 사용자 가져오기
            String userId = SecurityContextUtil.getCurrentUser().getUserId();
            String email = securityContextUtil.getCurrentUser().getEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + email));

            // 닉네임 변경
            if (dto.getNickname() != null) {
                boolean nicknameExists = userRepository.findByNickname(dto.getNickname()).isPresent();
                if (nicknameExists) {
                    return new CommonResDto(HttpStatus.BAD_REQUEST,400, "닉네임이 이미 존재합니다.", null,links);
                }
                user.setNickname(dto.getNickname());
                userRepository.save(user);
            }

            // 비밀번호 변경
            if (dto.getPassword() != null){
                String hashedPassword = passwordEncoder.encode(dto.getPassword());
                user.setPassword(hashedPassword);
                userRepository.save(user);
            }

            // 소개글(content) 변경
            if (dto.getContent() != null) {
                user.setContents(dto.getContent());
                userRepository.save(user);
            }

            // 이미지 업로드 처리
            if (img != null && !img.isEmpty()) {
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
            }
            return new CommonResDto(HttpStatus.OK,200, "사용자 정보가 성공적으로 수정되었습니다.", null,links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,400, "에러 발생: " + e.getMessage(), null,links);
        }
    }

////////////////////////////////// 친구 관리///////////////////////////////////////////////////////////////////////////////
    // 친구요청
    public CommonResDto addFriends(friendsDto dto) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends","/api/v1/users","Delete"));
        try {
            String email = securityContextUtil.getCurrentUser().getEmail();
            // Redis에 친구 요청 확인
            String reqKey = email;// 요청 키
            String resKey = dto.getResEmail(); // 응답 키
            
            // 친구대상이 있는지 확인
            User resUser = userRepository.findByEmail(resKey)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + resKey));
            // 이미 요청이 진행 중인지 확인
            if (Boolean.TRUE.equals(friendsTemplate.opsForSet().isMember(email, resKey))) {
                return new CommonResDto(HttpStatus.BAD_REQUEST,400, "이미 친구요청이 진행중입니다", null,links);
            }

            // Redis에 요청 저장 (요청자와 응답자 양쪽에 저장)
            friendsTemplate.opsForSet().add(reqKey, resKey);
            friendsTemplate.opsForSet().add(resKey, reqKey);

            return new CommonResDto(HttpStatus.OK,200, "친구요청 완료", null,links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,401, "에러발생: " + e.getMessage(), null,links);
        }
    }

    // 친구목록 조회
    public CommonResDto UserFriends() {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends","/api/v1/users","Delete"));
        try{
            String email = securityContextUtil.getCurrentUser().getEmail();
            List<Friends> friends = friendsRepository.findByUserEmail(email);

            // 친구 목록이 없을 경우 처리
            if (friends == null || friends.isEmpty()) {
                return new CommonResDto(HttpStatus.OK,200, "친구 목록이 비어 있습니다.", Collections.emptyList(),links);
            }
            // 닉네임 목록으로 변환
            List<String> friendemail = friends.stream()
                    .map(friend -> friend.getFriend().getEmail()) // 친구의 닉네임만 추출
                    .collect(Collectors.toList());

            return new CommonResDto(HttpStatus.OK,200, "친구목록 조회 성공", friendemail,links);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,400,"에러발생"+e.getMessage(),null,links);
        }
    }

    // 친구 삭제
    public CommonResDto deleteFriend(friendsDto dto) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends","/api/v1/users","Delete"));
        try {
            // 현재 사용자 정보 가져오기
            String email = securityContextUtil.getCurrentUser().getEmail();

            // 요청자(User) 조회
            User reqUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));

            // 응답자(User) 조회
            User resUser = userRepository.findByEmail(dto.getResEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " +dto.getResEmail()));

            // 요청자 -> 응답자 관계 삭제
            Friends friendToRemove = friendsRepository.findByUserEmailAndFriendEmail(email, resUser.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Friend relationship not found"));

            // 응답자 -> 요청자 관계 삭제
            Friends reverseFriendToRemove = friendsRepository.findByUserEmailAndFriendEmail(resUser.getEmail(),email)
                    .orElseThrow(() -> new IllegalArgumentException("Reverse friend relationship not found"));

            // 관계 삭제
            friendsRepository.delete(friendToRemove);
            friendsRepository.delete(reverseFriendToRemove);

            return new CommonResDto(HttpStatus.OK,200, "양방향 친구 관계가 삭제되었습니다.", null,links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,401, "에러 발생: " + e.getMessage(), null,links);
        }
    }

    // 요청응답
    @Transactional
    public CommonResDto addResFriends(friendsDto dto) {
        List<CommonResDto.Link> links = List.of(
                new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"),
                new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"),
                new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE")
        );

        try {
            String reqEmail = securityContextUtil.getCurrentUser().getEmail();
            String resEmail = dto.getResEmail();

            if (resEmail == null || resEmail.isEmpty()) {
                throw new IllegalArgumentException("Response email cannot be null or empty");
            }

            // 사용자 객체 로드
            User requester = userRepository.findByEmail(reqEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Requester not found: " + reqEmail));
            User responder = userRepository.findByEmail(resEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Responder not found: " + resEmail));

            // 친구 관계 생성
            Friends friend1 = new Friends(requester, responder);
            Friends friend2 = new Friends(responder, requester);

            friendsRepository.save(friend1);
            friendsRepository.save(friend2);

            // Redis에서 요청 제거
            friendsTemplate.opsForSet().remove(reqEmail, resEmail);
            friendsTemplate.opsForSet().remove(resEmail, reqEmail);

            log.info("Friends successfully added between {} and {}", reqEmail, resEmail);
            return new CommonResDto(HttpStatus.OK, 200, "친구가 되었습니다.", null, links);

        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "잘못된 입력: " + e.getMessage(), null, links);

        } catch (Exception e) {
            log.error("Unexpected error during friend addition: {}", e.getMessage(), e);
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 에러 발생: " + e.getMessage(), null, links);
        }
    }

    // 요청 목록 조회
    public CommonResDto addFriendsJoin() {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE"));

        // 현재 사용자 이메일 가져오기
        String email = securityContextUtil.getCurrentUser().getEmail();
        String reqKey = email; // 요청 리스트 키

        // Redis에서 Set 반환
        Set<String> memberSet = friendsTemplate.opsForSet().members(reqKey);

        // Set을 List로 변환
        List<String> members = new ArrayList<>(memberSet);

        // 응답 생성
        return new CommonResDto(HttpStatus.OK, 200, "친구 요청 조회를 완료했습니다.", members, links);
    }

////////////////////////////////// 서버 관리 ///////////////////////////////////////////////////////////////////////////////

    // 서버 가입 신청
    public CommonResDto addReqServer(ServerDto dto) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "Delete"));

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));

            // Redis에 서버 ID 추가
            if (dto.getServerId() == null || dto.getServerId() <= 0) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 서버 ID입니다.", null, links);
            }

            // Redis에 기존 값 확인
            Set<String> existingServerIds = serverTemplate.opsForSet().members(email);
            if (existingServerIds != null && existingServerIds.contains(String.valueOf(dto.getServerId()))) {
                return new CommonResDto(HttpStatus.OK, 200, "이미 가입이 진행 중입니다.", null, links);
            }

            // Redis에 서버 ID 추가
            serverTemplate.opsForSet().add(email, String.valueOf(dto.getServerId()));
            return new CommonResDto(HttpStatus.OK, 200, "서버 가입 요청이 완료되었습니다.", null, links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 가입 요청 중 오류가 발생했습니다.", e.getMessage(), links);
        }
    }

    // 가입된 서버 조회
    public CommonResDto userServerJoin() {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE"));

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            // Redis에서 특정 키의 모든 멤버 데이터 조회
            Set<String> allData = serverTemplate.opsForSet().members(email);

            if (allData == null || allData.isEmpty()) {
                return new CommonResDto(HttpStatus.OK, 200, "가입된 서버가 없습니다.", null, links);
            }

            // Set 데이터를 List로 변환
            List<ServerResDto> serverResList = new ArrayList<>();
            for (String data : allData) {
                serverResList.add(new ServerResDto(data, null));
            }

            // 반환할 응답 객체 생성
            return new CommonResDto(HttpStatus.OK, 200, "서버 데이터 조회 성공", serverResList, links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 조회 중 오류 발생", null, links);
        }
    }


    // 서버 탈퇴
     public CommonResDto deleteServer(long serverId) {
         List<CommonResDto.Link> links = new ArrayList<>();
         links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
         links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
         links.add(new CommonResDto.Link("DeleteFriends","/api/v1/users","Delete"));
        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            // 사용자 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));

            // 서버 리스트에서 특정 serverId를 가진 Servers 객체 찾기
            Servers serverToRemove = user.getServerList().stream()
                    .filter(server -> server.getServerId() == serverId)
                    .findFirst()
                    .orElse(null);

            if (serverToRemove == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST,400, "해당 serverId를 찾을 수 없습니다.", null,links);
            }

            // 서버 리스트에서 제거
            user.getServerList().remove(serverToRemove);

            // 변경된 엔티티 저장
            userRepository.save(user);

            // Servers 엔티티 삭제
            serverRepository.delete(serverToRemove);

            return new CommonResDto(HttpStatus.OK,200, "서버 삭제 성공", null,links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,400, "서버 삭제 중 에러 발생", e.getMessage(),links);
        }
    }
    // 서버 가입 응답
    public CommonResDto addResServer(ServerDto dto) {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addFriends", "/api/v1/users/friends", "POST"));
        links.add(new CommonResDto.Link("ListFriends", "/api/v1/users/friends", "GET"));
        links.add(new CommonResDto.Link("DeleteFriends", "/api/v1/users", "DELETE"));

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            log.info("Requester Email: {}", email);

            // 사용자 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));

            // AddStatus 검증
            if (dto.getAddStatus() == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "Status를 결정해주세요 (APPROVE 또는 REJECT)", null, links);
            }

            // Redis에서 serverId 가져오기
            String serverIdStr = serverTemplate.opsForSet().pop(email);
            if (serverIdStr == null || serverIdStr.isEmpty()) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "해당 요청에 대한 serverId가 없습니다.", null, links);
            }

            // 안전하게 Long 변환
            int serverId;
            try {
                serverId = Integer.parseInt(serverIdStr);
            } catch (NumberFormatException e) {
                log.error("Invalid serverId format in Redis: {}", serverIdStr, e);
                return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "Redis에 저장된 serverId 형식이 올바르지 않습니다.", null, links);
            }

            if ("APPROVE".equalsIgnoreCase(String.valueOf(dto.getAddStatus()))) {
                // 서버 생성 및 저장
                Servers server = new Servers();
                server.setServerId(serverId);
                server.setUser(user);

                user.getServerList().add(server);
                serverRepository.save(server);

                return new CommonResDto(HttpStatus.OK, 200, "서버 가입이 승인되었습니다.", null, links);
            } else if ("REJECT".equalsIgnoreCase(String.valueOf(dto.getAddStatus()))) {
                // 서버 가입 요청 거절
                serverTemplate.opsForSet().remove(email, String.valueOf(serverId));

                return new CommonResDto(HttpStatus.OK, 200, "서버 가입이 거절되었습니다.", null, links);
            }

            return new CommonResDto(HttpStatus.BAD_REQUEST, 400, "Invalid AddStatus", null, links);
        } catch (Exception e) {
            log.error("Error in addResServer: {}", e.getMessage(), e);
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 응답 처리 중 오류 발생", e.getMessage(), links);
        }
    }


    // 서버 가입 요청 조회
    public CommonResDto addServerJoin() {
        List<CommonResDto.Link> links = new ArrayList<>();
        links.add(new CommonResDto.Link("addServers", "/api/v1/users/servers", "POST"));
        links.add(new CommonResDto.Link("ListServers", "/api/v1/users/servers", "GET"));
        links.add(new CommonResDto.Link("DeleteServers", "/api/v1/users/servers", "DELETE"));

        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();

            // Redis에서 모든 요청 서버 ID 조회
            Set<String> allData = serverTemplate.opsForSet().members(email);

            if (allData == null || allData.isEmpty()) {
                return new CommonResDto(HttpStatus.OK, 200, "가입 요청된 서버가 없습니다.", null, links);
            }

            // 요청 데이터 처리
            List<ServerResDto> serverResList = allData.stream()
                    .map(serverId -> new ServerResDto(serverId, null))
                    .collect(Collectors.toList());

            return new CommonResDto(HttpStatus.OK, 200, "가입 요청 서버 목록 조회 성공", serverResList, links);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 요청 조회 중 오류 발생", null, links);
        }
    }


    // Redis 데이터 삭제 로직
    private void deleteRedisServerData(String reqEmail, Long serverId) {
        try {
            serverTemplate.opsForSet().remove(reqEmail, String.valueOf(serverId));
        } catch (Exception e) {
            // Redis 삭제 실패 시 로그 추가
            System.err.println("Redis 데이터 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    private boolean checkToken(String reqToken, String redisToken) {
        return reqToken.equals(redisToken);
    }

    ///feign 통신

//    public void findMember(String id) {
//
//        userRepository.fin
//    }
}


