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
                return new CommonResDto(HttpStatus.BAD_REQUEST,"잘못된요청입니다.",null);
            }
            User user = new User();
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setNickname(dto.getNickname());
            user.setCreatedAt(LocalDateTime.now());
//        user.setProfileImage(uploadFile(img));


            userRepository.save(user);

            return new CommonResDto(HttpStatus.OK, "회원가입을 환영합니다.", null);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }

    // 로그인로직
    public CommonResDto userSignIn(UserLoginReqDto dto) {

        if (loginTemplate.opsForValue().get(dto.getEmail())!=null){
            return new CommonResDto(HttpStatus.BAD_REQUEST,"이미 로그인 중입니다.",null);
        }
        if(dto.getEmail()==null||dto.getPassword()==null){
            return new CommonResDto(HttpStatus.BAD_REQUEST,"잚못된 요청입니다.",null);
        }
        User user =userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + dto.getEmail()));
        log.info(user.toString());
        try{
            if (user == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, "Invalid email.", null);
            }
            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, "Invalid password.", null);
            }
            //mysql에 refresh토큰 저장
            String refreshToken = jwtTokenProvider.refreshToken(dto.getEmail(),user.getId());
            user.setRefreshToken(refreshToken);

            userRepository.save(user);

            //redis에 accesstoken 저장
            String accessToken = jwtTokenProvider.accessToken(user.getEmail(),user.getId());
            loginTemplate.opsForValue().set(dto.getEmail(), accessToken);


            return new CommonResDto(HttpStatus.OK, "SignUp successfully.", accessToken);
        } catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
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

            return new CommonResDto(HttpStatus.OK, "SignOut successfully.", null);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"로그아웃중 에러 발생",e.getMessage());
        }

    }

    //토큰갱신
    public CommonResDto refreshToken() {
        //토큰 유효성 검사
        try{
                String email = securityContextUtil.getCurrentUser().getEmail();
                log.info(email);
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + email));
                Long userId = user.getId();
            // 리프레쉬 토큰 유효성 검사
            String refreshToken = user.getRefreshToken();
            boolean flag = jwtUtil.isTokenExpired(refreshToken);
            log.info(refreshToken);
            log.info(String.valueOf(flag));
            if (!flag) {
                String newAccessToken = jwtTokenProvider.accessToken(email, userId);
                log.info(newAccessToken);
                loginTemplate.delete(email);
                loginTemplate.opsForValue().set(email, newAccessToken);
                return new CommonResDto(HttpStatus.OK, "Refresh token successfully.", newAccessToken);
            }else{
                return new CommonResDto(HttpStatus.BAD_REQUEST, "Invalid refresh token.", null);
            }
        }catch(Exception e){
            e.printStackTrace();
                 return new CommonResDto(HttpStatus.BAD_REQUEST,"이메일 access token error",null);
        }
    }

    // 인증 이메일 전송 로직 인증번호 10분 유효 (이메일 만 필요)
    public CommonResDto sendVerificationEmail(EmailCheckDto dto) {

        if(dto.getEmail()==null){
            return new CommonResDto(HttpStatus.BAD_REQUEST,"잚못된 요청입니다.",null);
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


            return new CommonResDto(HttpStatus.OK,"이메일 전송 성공!!!", null);
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"이메일 전송 실패",null);
        }
    }

    // 이메일 인증, 비밀번호 회원가입시
    public CommonResDto confirm(String email, String token) {
       try{
           if (email== null && token==null){
               return new CommonResDto(HttpStatus.BAD_REQUEST,"잘못된 요청입니다.",null);
           }
           String redisEmail = checkTemplate.opsForValue().get(token);

           // 비밀번호 찾기 이메일 인증
           if(!redisEmail.isEmpty() && userRepository.findByEmail(email).isPresent()) {
                if (redisEmail.equals(email)) {
                    checkTemplate.delete(token);
                    return new CommonResDto(HttpStatus.OK, "token 유효, 비밀번호를 수정해주세요", null);
                }
           //회원가입 미메일 인증     
            }else if(!redisEmail.isEmpty()&&!userRepository.findByEmail(email).isPresent()) {
               if (redisEmail.equals(email)) {
                   checkTemplate.delete(token);
                   return new CommonResDto(HttpStatus.OK, "token 유효, 회원가입을 계속 진행하세요",null);
               }
               return new CommonResDto(HttpStatus.BAD_REQUEST, "token 유효하지 않음, 재 인증해주세요", "null");
            }
           return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"토큰이 저장되지 않았습니다",null);
        }catch (Exception e){
           e.printStackTrace();
           return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
       }
    }

    // 이메일 &닉네임 중복검사
    public CommonResDto duplicateCheck(String email, String nickname) {
        try {
            // 이메일 중복 체크
            if (email != null) {
                boolean emailExists = userRepository.findByEmail(email).isPresent();
                return emailExists
                        ? new CommonResDto(HttpStatus.OK, "이메일 사용 불가", null)
                        : new CommonResDto(HttpStatus.OK, "이메일을 사용해도 좋아요.", null);
            }

            // 닉네임 중복 체크
            if (nickname != null) {
                boolean nicknameExists = userRepository.findByNickname(nickname).isPresent();
                return nicknameExists
                        ? new CommonResDto(HttpStatus.OK, "닉네임 사용 불가", null)
                        : new CommonResDto(HttpStatus.OK, "닉네임을 사용해도 좋아요.", null);
            }

            // 이메일과 닉네임 모두 null인 경우
            return new CommonResDto(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러 발생", e.getMessage());
        }
    }

    //회원탈퇴
    public CommonResDto delete() {
        try{
            String email = securityContextUtil.getCurrentUser().getEmail();
            userRepository.deleteByEmail(email);
            loginTemplate.delete(email);
            return new CommonResDto(HttpStatus.OK, "삭제완료", null);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }

// 회원정보수정
    public CommonResDto modify(ModifyDto dto, MultipartFile img) {
        try {
            // 현재 인증된 사용자 가져오기
            Long userId = SecurityContextUtil.getCurrentUser().getUserId();
            String email = securityContextUtil.getCurrentUser().getEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + email));

            // 닉네임 변경
            if (dto.getNickname() != null) {
                boolean nicknameExists = userRepository.findByNickname(dto.getNickname()).isPresent();
                if (nicknameExists) {
                    return new CommonResDto(HttpStatus.BAD_REQUEST, "닉네임이 이미 존재합니다.", null);
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
            return new CommonResDto(HttpStatus.OK, "사용자 정보가 성공적으로 수정되었습니다.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러 발생: " + e.getMessage(), null);
        }
    }

////////////////////////////////// 친구 관리///////////////////////////////////////////////////////////////////////////////
    // 친구요청
    public CommonResDto addFriends(String resEmail) {
        try {
            String email = securityContextUtil.getCurrentUser().getEmail();
            // Redis에 친구 요청 확인
            String reqKey = email;// 요청 키
            String resKey = resEmail; // 응답 키
            
            // 친구대상이 있는지 확인
            User resUser = userRepository.findByEmail(resEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + resEmail));
            // 이미 요청이 진행 중인지 확인
            if (Boolean.TRUE.equals(friendsTemplate.opsForSet().isMember(email, resEmail))) {
                return new CommonResDto(HttpStatus.OK, "이미 친구요청이 진행중입니다", null);
            }

            // Redis에 요청 저장 (요청자와 응답자 양쪽에 저장)
            friendsTemplate.opsForSet().add(reqKey, resKey);
            friendsTemplate.opsForSet().add(resKey, reqKey);

            return new CommonResDto(HttpStatus.OK, "친구요청 완료", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러발생: " + e.getMessage(), null);
        }
    }

    // 친구목록 조회
    public CommonResDto UserFriends() {
        try{
            String email = securityContextUtil.getCurrentUser().getEmail();
            List<Friends> friends = friendsRepository.findByUserEmail(email);

            // 친구 목록이 없을 경우 처리
            if (friends == null || friends.isEmpty()) {
                return new CommonResDto(HttpStatus.OK, "친구 목록이 비어 있습니다.", Collections.emptyList());
            }
            // 닉네임 목록으로 변환
            List<String> friendemail = friends.stream()
                    .map(friend -> friend.getFriend().getEmail()) // 친구의 닉네임만 추출
                    .collect(Collectors.toList());

            return new CommonResDto(HttpStatus.OK, "친구목록 조회 성공", friendemail);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }

    // 친구 삭제
    public CommonResDto deleteFriend(String resEmail) {
        try {
            // 현재 사용자 정보 가져오기
            String email = securityContextUtil.getCurrentUser().getEmail();

            // 요청자(User) 조회
            User reqUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));

            // 응답자(User) 조회
            User resUser = userRepository.findByEmail(resEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " +resEmail));

            // 요청자 -> 응답자 관계 삭제
            Friends friendToRemove = friendsRepository.findByUserEmailAndFriendEmail(email, resUser.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Friend relationship not found"));

            // 응답자 -> 요청자 관계 삭제
            Friends reverseFriendToRemove = friendsRepository.findByUserEmailAndFriendEmail(resUser.getEmail(),email)
                    .orElseThrow(() -> new IllegalArgumentException("Reverse friend relationship not found"));

            // 관계 삭제
            friendsRepository.delete(friendToRemove);
            friendsRepository.delete(reverseFriendToRemove);

            return new CommonResDto(HttpStatus.OK, "양방향 친구 관계가 삭제되었습니다.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러 발생: " + e.getMessage(), null);
        }
    }


    public CommonResDto addResFriends(friendsDto dto) {
        try {
            String email = securityContextUtil.getCurrentUser().getEmail();

            String reqKey = email; // 요청 리스트 키
            String resKey =  dto.getResEmail(); // 응답 리스트 키



            // Mysql로 이관
            // 양방향 친구 관계 저장
            Friends friend1 = new Friends(reqKey, resKey);
            Friends friend2 = new Friends(resKey, reqKey);

            friendsRepository.save(friend1);
            friendsRepository.save(friend2);

            // 요청 리스트에서 제거
            friendsTemplate.opsForSet().remove(reqKey, resKey);
            friendsTemplate.opsForSet().remove(resKey,reqKey);

            return new CommonResDto(HttpStatus.OK, "친구가 되었습니다.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러발생: " + e.getMessage(), null);
        }
    }

    public CommonResDto addFriendsJoin() {
        String email = securityContextUtil.getCurrentUser().getEmail();
        String reqKey = email; // 요청 리스트 키

        List<String> members = (List<String>) friendsTemplate.opsForSet().members(reqKey);
        return new CommonResDto(HttpStatus.OK,"친구 요청 조회를 완료했습니다.", members );
    }
////////////////////////////////// 서버 관리 ///////////////////////////////////////////////////////////////////////////////

    // 서버 가입 신청
    public CommonResDto addReqServer(long serverId) {
        String email = securityContextUtil.getCurrentUser().getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));

        long redisServerId  = Long.parseLong(serverTemplate.opsForValue().get(email));

        if(serverId == redisServerId){
            return new CommonResDto(HttpStatus.OK, "이미 가입이 진행중입니다.", null);
        }

        if (user.getServerList().contains(serverId)) {
            return new CommonResDto(HttpStatus.OK,"이미 서버에 가입이 되어 있습니다,", null);
        }

        serverTemplate.opsForSet().add(email, String.valueOf(serverId));
        return new CommonResDto(HttpStatus.OK, "서버 가입 요청이 완료되었습니다.",null);
    }

    // 가입된 서버 조회
    public CommonResDto userServerJoin() {
        String email = securityContextUtil.getCurrentUser().getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));
        List<Servers> serverList = user.getServerList();

        return new CommonResDto(HttpStatus.OK,"서바 조회 완료", serverList);
    }

    // 서버 탈퇴
     public CommonResDto deleteServer(long serverId) {
        try {
            String email = securityContextUtil.getCurrentUser().getEmail();
            // 사용자 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));

            // 서버 리스트에서 특정 serverId를 가진 Servers 객체 찾기
            Servers serverToRemove = user.getServerList().stream()
                    .filter(server -> server.getServerId() == serverId)
                    .findFirst()
                    .orElse(null);

            if (serverToRemove == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, "해당 serverId를 찾을 수 없습니다.", null);
            }

            // 서버 리스트에서 제거
            user.getServerList().remove(serverToRemove);

            // 변경된 엔티티 저장
            userRepository.save(user);

            // Servers 엔티티 삭제
            serverRepository.delete(serverToRemove);

            return new CommonResDto(HttpStatus.OK, "서버 삭제 성공", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "서버 삭제 중 에러 발생", e.getMessage());
        }
    }
    // 서버 가입 응답
    public CommonResDto addResServer(long serverId, String status) {
        try {
            String email = securityContextUtil.getCurrentUser().getEmail();
            // 사용자 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));

            // AddStatus 검증
            if (status == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, "Status를 결정해주세요 Approve or Rejection", null);
            }

            // Redis에서 serverId 가져오기
            String serverIdStr = serverTemplate.opsForValue().get(email);
            if (serverIdStr == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, "해당 요청에 대한 serverId가 Redis에 없습니다.", null);
            }
            int serverIds = Integer.parseInt(serverIdStr);

            if (status == "APPROVE") {
                // 서버 생성 및 저장
                Servers server = new Servers();
                server.setServerId(serverIds);
                server.setUser(user); // 관계 설정

                user.getServerList().add(server); // User의 서버 리스트에 추가
                serverRepository.save(server);   // 서버 저장

                // Redis 데이터 삭제
                deleteRedisServerData(email,  serverId);

                return new CommonResDto(HttpStatus.OK, "서버 가입에 승인되었습니다.", null);
            } else if (status == "REJECT") {
                // Redis 데이터 삭제
                deleteRedisServerData(email, serverId);

                return new CommonResDto(HttpStatus.OK, "서버 가입에 거절되었습니다.", null);
            }

            return new CommonResDto(HttpStatus.BAD_REQUEST, "Invalid AddStatus", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러 발생!!", e.getMessage());
        }
    }

    //서버 가입 요청 조회
    public CommonResDto addServerJoin() {
        try {
            String email = SecurityContextUtil.getCurrentUser().getEmail();
            // Redis에서 특정 키의 모든 멤버 데이터 조회

            Set<String> allData = serverTemplate.opsForSet().members(email);

            // Set 데이터를 List로 변환
            List<ServerResDto> serverResList = new ArrayList<>();
            if (allData != null) { // Null check for safety
                for (Object data : allData) {
                    // ServerResDto 생성 시, key와 value에 동일한 값을 넣거나 null 처리
                    serverResList.add(new ServerResDto(data.toString(), null));
                }
            }



            // 반환할 응답 객체 생성
            return new CommonResDto(HttpStatus.OK, "서버 데이터 조회 성공", serverResList);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 데이터 조회 중 에러 발생", null);
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


}


