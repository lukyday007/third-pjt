package com.singlebungle.backend.domain.user.service;

import com.singlebungle.backend.domain.user.dto.request.AuthRequestDTO;
import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.domain.user.repository.UserRepository;
import com.singlebungle.backend.global.auth.TokenInfo;
import com.singlebungle.backend.global.auth.dto.TokenResponseDTO;
import com.singlebungle.backend.global.auth.entity.JwtToken;
import com.singlebungle.backend.global.auth.auth.JwtProvider;
import com.singlebungle.backend.global.auth.repository.JwtTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService implements AuthService {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final JwtTokenRepository jwtTokenRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    // Google 로그인 또는 회원가입 처리
    @Override
    public TokenResponseDTO handleLoginOrSignup(String code, String redirectUri) {
        log.info("Handling login or signup with Google for authorization code: {}", code);

        // Google로부터 액세스 토큰 요청
        String googleAccessToken = getGoogleAccessToken(code, redirectUri);
        log.info("Access token received from Google: {}", googleAccessToken);

        // Google 사용자 정보 요청
        AuthRequestDTO userInfo = getGoogleUserInfo(googleAccessToken);
        log.info("User information from Google: email={}, name={}", userInfo.getEmail(), userInfo.getNickname());

        // 사용자 처리
        return processUser(userInfo, redirectUri);
    }

    private TokenResponseDTO processUser(AuthRequestDTO userInfo, String redirectUri) {
        Optional<User> existingUser = userRepository.findUserByEmail(userInfo.getEmail());

        User user; // User 객체를 메소드 내에서 선언
        TokenInfo tokenInfo;

        if (existingUser.isPresent()) {
            user = existingUser.get(); // 기존 사용자 가져오기
            tokenInfo = jwtProvider.generateAndSaveToken(user, redirectUri); // 토큰 생성 및 저장
        } else {
            log.info("No existing user found. Proceeding with signup.");
            user = createUser(userInfo); // 새로운 사용자 생성
            log.info("User successfully signed up with Google: {}", user);
            tokenInfo = jwtProvider.generateAndSaveToken(user, redirectUri); // 토큰 생성 및 저장
        }

        return new TokenResponseDTO(tokenInfo.getAccessToken(), tokenInfo.getRefreshToken());
    }

//    private TokenResponseDTO saveTokensAndReturnResponse(User user, TokenInfo tokenInfo, String redirectUri) {
//        String accessToken = tokenInfo.getAccessToken();
//        String refreshToken = tokenInfo.getRefreshToken();
//        log.info("Tokens issued: AccessToken={}, RefreshToken={}", accessToken, refreshToken);
//
//        saveJwtToken(user, accessToken, refreshToken, redirectUri);
//
//        return new TokenResponseDTO(accessToken, refreshToken);
//    }

    private User createUser(AuthRequestDTO userInfo) {
        AuthRequestDTO authRequestDTO = AuthRequestDTO.builder()
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .profileImagePath(userInfo.getProfileImagePath())
                .build();

        return userService.oauthSignup(authRequestDTO);
    }

    // 리다이렉트 URL 생성 메소드
    @Override
    public String generateLoginUrl(String redirectUri) {
        String loginUrl = "https://accounts.google.com/o/oauth2/auth?client_id=" + googleClientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=openid%20email%20profile";
        log.info("Generated Google login URL: {}", loginUrl);
        return loginUrl;
    }

    // Google Access Token 요청
    private String getGoogleAccessToken(String code, String redirectUri) {
        log.info("Requesting access token from Google for authorization code: {}", code);

        String tokenUrl = "https://oauth2.googleapis.com/token";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        String accessToken = response.getBody().get("access_token").toString();
        log.info("Access token received from Google: {}", accessToken);

        return accessToken;
    }

    // Google 사용자 정보 요청
    private AuthRequestDTO getGoogleUserInfo(String accessToken) {
        log.info("Requesting Google user information using access token: {}", accessToken);

        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

        String email = response.getBody().get("email").toString();
        String name = response.getBody().get("name").toString();
        String profileImagePath = response.getBody().get("picture").toString();

        return AuthRequestDTO.builder()
                .email(email)
                .nickname(name)
                .profileImagePath(profileImagePath)
                .build();
    }

//    // JWT 토큰 저장 메소드
//    private void saveJwtToken(User user, String accessToken, String refreshToken, String redirectUri) {
//        log.info("Saving jwt token for user: {}", user);
//        // 새로운 JwtToken 객체를 생성합니다.
//        JwtToken jwtTokenEntity = JwtToken.builder()
//                .userId(user.getUserId())
//                .redirectUri(redirectUri)
//                .accessToken(accessToken) // 액세스 토큰 설정
//                .refreshToken(refreshToken) // 리프레시 토큰 설정
//                .accessExpirationTime(System.currentTimeMillis() + jwtProvider.getAccessTokenExpirationTime()) // 액세스 토큰 만료 시간 설정
//                .refreshExpirationTime(System.currentTimeMillis() + jwtProvider.getRefreshTokenExpirationTime()) // 리프레시 토큰 만료 시간 설정
//                .build();
//
//        jwtTokenRepository.save(jwtTokenEntity);
//    }
}
