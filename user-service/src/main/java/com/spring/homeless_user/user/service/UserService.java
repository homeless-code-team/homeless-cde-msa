package com.spring.homeless_user.user.service;


import com.spring.homeless_user.common.auth.JwtTokenProvider;
import com.spring.homeless_user.common.utill.JwtUtil;
import com.spring.homeless_user.user.dto.*;
import com.spring.homeless_user.user.entity.AddStatus;
import com.spring.homeless_user.user.entity.Friends;
import com.spring.homeless_user.user.entity.Servers;
import com.spring.homeless_user.user.entity.User;
import com.spring.homeless_user.user.repository.FriendsRepository;
import com.spring.homeless_user.user.repository.ServerRepository;
import com.spring.homeless_user.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserService {


    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final  FriendsRepository friendsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final  JavaMailSender mailSender;
    private final JwtUtil jwtUtil;
//    private final AmazonS3 amazonS3;
    @Qualifier("login")
    private final RedisTemplate<String, String> loginRedis;
    @Qualifier("friends")
    private final RedisTemplate<String, String> friendsRedis;
    @Qualifier("server")
    private final RedisTemplate<String, String> serverRedis;
    @Qualifier("signup")
    private final RedisTemplate<String, String> signupRedis;

    public UserService(UserRepository userRepository,
                       ServerRepository serverRepository,
                       FriendsRepository friendsRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JavaMailSender mailSender, JwtUtil jwtUtil,
                       @Qualifier("login") RedisTemplate<String, String> loginRedis,
                       @Qualifier("friends") RedisTemplate<String, String> friendsRedis,
                       @Qualifier("server") RedisTemplate<String, String> serverRedis,
                       @Qualifier("signup") RedisTemplate signupRedis) {
        this.userRepository = userRepository;
        this.serverRepository = serverRepository;
        this.friendsRepository = friendsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
        this.loginRedis = loginRedis;
        this.friendsRedis = friendsRedis;
        this.serverRedis = serverRedis;
        this.signupRedis = signupRedis;
    }


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
            User user = new User();
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setNickname(dto.getNickname());
            user.setCreateAt(LocalDateTime.now());
//        user.setProfileImage(uploadFile(img));


            userRepository.save(user);

            return new CommonResDto(HttpStatus.OK, "회원가입을 환영합니다.", null);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }

    private CommonResDto sendVerificationEmail(String email) {

        String token = UUID.randomUUID().toString().trim();

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
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(text, true); // HTML 형식으로 전송

            // 이메일 전송
            mailSender.send(message);

            return new CommonResDto(HttpStatus.OK,"이메일 전송 성공!!!", null);
        } catch (Exception e) {
            // 예외 처리
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"이메일 전송 실패",null);
        }
    }

    //회원가입 인증로직
    public CommonResDto confirm(String email, String token) {
       try{
            signupRedis.opsForValue().get(email);
            String cachedToken = (String) signupRedis.opsForValue().get(token);
            if (cachedToken != null && cachedToken.equals(email)) {
                signupRedis.delete(email); // 토큰 검증 후 삭제
                return new CommonResDto(HttpStatus.OK, "token okay", "null");
            }
            return new CommonResDto(HttpStatus.OK, "token not okay", "null");
        }catch (Exception e){
           e.printStackTrace();
           return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
       }
    }

    
    // 로그인로직
    public CommonResDto userSignIn(UserLoginReqDto dto) {
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
            String refreshToken = jwtTokenProvider.refreshToken(dto.getEmail());
            user.setRefreshToken(refreshToken);

            userRepository.save(user);

            //redis에 accesstoken 저장
            String accessToken = jwtTokenProvider.accessToken(user.getEmail());
            loginRedis.opsForValue().set(dto.getEmail(), accessToken);


            return new CommonResDto(HttpStatus.OK, "SignUp successfully.", accessToken);
        } catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }


    public CommonResDto duplicateEmail(String email) {
        try {
            boolean exists = userRepository.findByEmail(email).isPresent();
            if (exists) {
                return new CommonResDto(HttpStatus.OK, "이메일 사용 불가", null);
            } else {
                return new CommonResDto(HttpStatus.OK, "이메일을 사용해도 좋아요.", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러발생", e.getMessage());
        }
    }

    public CommonResDto duplicateNickname(String nickname) {
        try {
            boolean exists = userRepository.findByNickname(nickname).isPresent();
            if (exists) {
                return new CommonResDto(HttpStatus.OK, "이메일 사용 불가", null);
            } else {
                return new CommonResDto(HttpStatus.OK, "이메일을 사용해도 좋아요.", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러발생", e.getMessage());
        }
    }

    public CommonResDto pwChange(UserLoginReqDto dto) {
       try{
        User user =userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + dto.getEmail()));
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
        return new CommonResDto(HttpStatus.OK, "비밀번호 변경 성공", null);
       }catch (Exception e){
           e.printStackTrace();
           return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
       }
    }


    public CommonResDto quit(String email) {
        try{
            userRepository.deleteByEmail(email);
            return new CommonResDto(HttpStatus.OK, "삭제완료", null);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }

    public CommonResDto profileImageUpdate(String email,MultipartFile img) throws IOException {
        try{
            User user =userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " +email));
//        user.setProfileImage(uploadFile(img));
            return new CommonResDto(HttpStatus.OK, "이미지 변경 완료", null);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러 발생: " + e.getMessage(), null);
        }
    }

    public CommonResDto checkEmail(EmailCheckDto dto) {
        try{
            if (signupRedis.opsForValue().get(dto.getEmail()) != null && dto.getToken().equals(signupRedis.opsForValue().get(dto.getEmail()))){
                signupRedis.delete(dto.getEmail());
                return new CommonResDto(HttpStatus.OK, "인증번호가 일치합니다.",null);
            }
            return new CommonResDto(HttpStatus.OK, "인증번호가 일치하지 안습니다.", null);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러 발생: " + e.getMessage(), null);
        }
    }

    public CommonResDto modify(String nickname) {
        try{
            User user =userRepository.findByNickname(nickname)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + nickname));
            user.setNickname(nickname);
            userRepository.save(user);
            return new CommonResDto(HttpStatus.OK, "닉네임 변경 성공 ", null);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }
////////////////////////////////// 친구 관리///////////////////////////////////////////////////////////////////////////////
    public CommonResDto addFriends(friendsDto dto) {
        try {
            // Redis에 친구 요청 확인
            String reqKey = dto.getReqEmail(); // 요청 키
            String resKey = dto.getResEmail(); // 응답 키

            // 이미 요청이 진행 중인지 확인
            if (friendsRedis.opsForSet().isMember(reqKey, dto.getResEmail())) {
                return new CommonResDto(HttpStatus.OK, "이미 친구요청이 진행중입니다", null);
            }

            // Redis에 요청 저장 (요청자와 응답자 양쪽에 저장)
            friendsRedis.opsForSet().add(reqKey, dto.getReqEmail());
            friendsRedis.opsForSet().add(resKey, dto.getReqEmail());

            return new CommonResDto(HttpStatus.OK, "친구요청 완료", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러발생: " + e.getMessage(), null);
        }
    }


    public CommonResDto UserFriends(String email) {
        try{
            User user =userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + email));
            Long userId = user.getId();
            List<Friends> friends = friendsRepository.findAllById(userId);

            // 친구 목록이 없을 경우 처리
            if (friends == null || friends.isEmpty()) {
                return new CommonResDto(HttpStatus.OK, "친구 목록이 비어 있습니다.", Collections.emptyList());
            }
            // 닉네임 목록으로 변환
            List<String> friendNicknames = friends.stream()
                    .map(friend -> friend.getFriend().getNickname()) // 친구의 닉네임만 추출
                    .collect(Collectors.toList());

            return new CommonResDto(HttpStatus.OK, "친구목록 조회 성공", friendNicknames);
        }catch (Exception e){
            e.printStackTrace();
            return new  CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR,"에러발생"+e.getMessage(),null);
        }
    }

    public CommonResDto deleteFriend(friendsDto dto) {
        try {
            // 요청자(User) 조회
            User reqUser = userRepository.findByEmail(dto.getReqEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + dto.getReqEmail()));

            // 응답자(User) 조회
            User resUser = userRepository.findByEmail(dto.getResEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + dto.getResEmail()));

            // 요청자의 친구 관계 찾기 (reqUser -> resUser)
            Friends friendshipReqToRes = friendsRepository.findByUserAndFriend(reqUser, resUser)
                    .orElse(null);

            // 응답자의 친구 관계 찾기 (resUser -> reqUser)
            Friends friendshipResToReq = friendsRepository.findByUserAndFriend(resUser, reqUser)
                    .orElse(null);

            // 둘 중 하나라도 없는 경우 처리
            if (friendshipReqToRes == null || friendshipResToReq == null) {
                return new CommonResDto(HttpStatus.OK, "이미 친구 관계가 아닙니다.", null);
            }

            // 양방향 친구 관계 삭제
            friendsRepository.delete(friendshipReqToRes);
            friendsRepository.delete(friendshipResToReq);

            return new CommonResDto(HttpStatus.OK, "양방향 친구 관계가 삭제되었습니다.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러 발생: " + e.getMessage(), null);
        }
    }


    public CommonResDto addResFriends(friendsDto dto) {
        try {
            String reqKey = dto.getReqEmail(); // 요청 리스트 키
            String resKey =  dto.getResEmail(); // 응답 리스트 키
            String friendKey1 =  dto.getReqEmail(); // 요청자의 친구 리스트 키
            String friendKey2 =  dto.getResEmail(); // 응답자의 친구 리스트 키

            // 요청자와 응답자 정보 조회
            User reqUser = userRepository.findByEmail(dto.getReqEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + dto.getReqEmail()));

            User resUser = userRepository.findByEmail(dto.getResEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + dto.getReqEmail()));;


            // Mysql로 이관
            // 양방향 친구 관계 저장
            Friends friend1 = new Friends(reqUser, resUser);
            Friends friend2 = new Friends(resUser,reqUser);

            friendsRepository.save(friend1);
            friendsRepository.save(friend2);

            // 요청 리스트에서 제거
            friendsRedis.opsForSet().remove(reqKey, dto.getResEmail());
            friendsRedis.opsForSet().remove(resKey, dto.getReqEmail());

            return new CommonResDto(HttpStatus.OK, "친구가 되었습니다.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러발생: " + e.getMessage(), null);
        }
    }

    public CommonResDto addFriendsJoin(String email) {
        String reqKey = email; // 요청 리스트 키
        String friendKey1 = email; // 요청자의 친구 리스트 키

        List<String> members = (List<String>) friendsRedis.opsForSet().members(reqKey);
        return new CommonResDto(HttpStatus.OK,"친구 요청 조회를 완료했습니다.", members );
    }
////////////////////////////////// 서버 관리 ///////////////////////////////////////////////////////////////////////////////

    // 서버 가입 신청
    public CommonResDto addReqServer(ServerDto dto) {
        User user = userRepository.findByEmail(dto.getReqEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + dto.getReqEmail()));

        int serverid  = Integer.parseInt(serverRedis.opsForValue().get(dto.getReqEmail()));

        if(serverid == dto.getServerId()){
            return new CommonResDto(HttpStatus.OK, "이미 가입이 진행중입니다.", null);
        }

        if (user.getServerList().contains(dto.getServerId())) {
            return new CommonResDto(HttpStatus.OK,"이미 서버에 가입이 되어 있습니다,", null);
        }

        serverRedis.opsForHash().hasKey(dto.getReqEmail(), String.valueOf(dto.getServerId()));
        return new CommonResDto(HttpStatus.OK, "서버 가입 요청이 완료되었습니다.",null);
    }

    // 가입된 서버 조회
    public CommonResDto userServerJoin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + email));
        List<Servers> serverList = user.getServerList();

        return new CommonResDto(HttpStatus.OK,"서바 조회 완료", serverList);
    }

    // 서버 탈퇴
     public CommonResDto deleteServer(ServerDto dto) {
        try {
            // 사용자 조회
            User user = userRepository.findByEmail(dto.getReqEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + dto.getReqEmail()));

            // 서버 리스트에서 특정 serverId를 가진 Servers 객체 찾기
            Servers serverToRemove = user.getServerList().stream()
                    .filter(server -> server.getServerId() == dto.getServerId())
                    .findFirst()
                    .orElse(null);

            if (serverToRemove == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, "해당 serverId를 찾을 수 없습니다.", null);
            }

            // 서버 리스트에서 제거
            user.getServerList().remove(serverToRemove);

            // Servers 엔티티 삭제
            serverRepository.delete(serverToRemove);

            return new CommonResDto(HttpStatus.OK, "서버 삭제 성공", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "서버 삭제 중 에러 발생", e.getMessage());
        }
    }
    // 서버 가입 응답
    public CommonResDto addResServer(ServerDto dto) {
        try {
            // 사용자 조회
            User user = userRepository.findByEmail(dto.getReqEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid requestEmail: " + dto.getReqEmail()));

            // AddStatus 검증
            if (dto.getAddStatus() == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, "Status를 결정해주세요 Approve or Rejection", null);
            }

            // Redis에서 serverId 가져오기
            String serverIdStr = serverRedis.opsForValue().get(dto.getReqEmail());
            if (serverIdStr == null) {
                return new CommonResDto(HttpStatus.BAD_REQUEST, "해당 요청에 대한 serverId가 Redis에 없습니다.", null);
            }
            int serverId = Integer.parseInt(serverIdStr);

            if (dto.getAddStatus() == AddStatus.Approved) {
                // 서버 생성 및 저장
                Servers server = new Servers();
                server.setServerId(serverId);
                server.setUser(user); // 관계 설정

                user.getServerList().add(server); // User의 서버 리스트에 추가
                serverRepository.save(server);   // 서버 저장

                // Redis 데이터 삭제
                deleteRedisServerData(dto.getReqEmail(), serverId);

                return new CommonResDto(HttpStatus.OK, "서버 가입에 승인되었습니다.", null);
            } else if (dto.getAddStatus() == AddStatus.Rejected) {
                // Redis 데이터 삭제
                deleteRedisServerData(dto.getReqEmail(), serverId);

                return new CommonResDto(HttpStatus.OK, "서버 가입에 거절되었습니다.", null);
            }

            return new CommonResDto(HttpStatus.BAD_REQUEST, "Invalid AddStatus", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "에러 발생!!", e.getMessage());
        }
    }

    //서버 가입 요청 조회
    public CommonResDto addServerJoin(String email) {
        try {
            // Redis에서 특정 키의 모든 필드-값 데이터 조회
            Map<Object, Object> allData = serverRedis.opsForHash().entries(email);

            // Map 데이터를 List로 변환
            List<ServerResDto> serverResList = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : allData.entrySet()) {
                serverResList.add(new ServerResDto(entry.getKey().toString(), entry.getValue().toString()));
            }

            // 반환할 응답 객체 생성
            return new CommonResDto(HttpStatus.OK, "서버 데이터 조회 성공", serverResList);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResDto(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 데이터 조회 중 에러 발생", null);
        }
    }


    // Redis 데이터 삭제 로직
    private void deleteRedisServerData(String reqEmail, int serverId) {
        try {
            serverRedis.opsForHash().delete(reqEmail, String.valueOf(serverId));
        } catch (Exception e) {
            // Redis 삭제 실패 시 로그 추가
            System.err.println("Redis 데이터 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    public CommonResDto refreshToken(String accessToken) {
        // Bearer 검증 및 공백 제거
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7).trim(); // "Bearer " 제거 후 공백 제거
        }

        // JWT 파싱 및 검증
        jwtUtil.extractAllClaims(accessToken);
        String emailFromToken = jwtUtil.getEmailFromToken(accessToken);

        // Redis 토큰 확인
        String redisToken = loginRedis.opsForValue().get(emailFromToken);
        if (redisToken == null) {
            log.warn("No token found in Redis for email: {}", emailFromToken);
           return new CommonResDto(HttpStatus.UNAUTHORIZED, "Token not found in Redis",null);
        }
        if (!checkToken(accessToken, redisToken)) {
            log.warn("Token mismatch for email: {}", emailFromToken);
           return new CommonResDto (HttpStatus.UNAUTHORIZED, "Token mismatch", null);
        }
        User user = userRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + emailFromToken));

        if (!jwtTokenProvider.validateRefreshToken(user.getRefreshToken())) {
            return new CommonResDto(HttpStatus.UNAUTHORIZED,"REfreshToken Expired",null);
        }
        String newAccessToken = jwtTokenProvider.accessToken(emailFromToken);
        return  new CommonResDto(HttpStatus.OK,"accessToken 갱신완료!!!", newAccessToken);
    }
    private boolean checkToken(String reqToken, String redisToken) {
        return reqToken.equals(redisToken);
    }
}


