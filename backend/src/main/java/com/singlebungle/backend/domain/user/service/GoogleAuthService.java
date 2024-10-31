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

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    // Google 로그인 또는 회원가입 처리
    @Override
    public TokenResponseDTO handleLoginOrSignup(String code) {
        log.info("Handling login or signup with Google for authorization code: {}", code);

        // Google로부터 액세스 토큰 요청
        String googleAccessToken = getGoogleAccessToken(code);
        log.info("Access token received from Google: {}", googleAccessToken);

        // Google 사용자 정보 요청
        AuthRequestDTO userInfo = getGoogleUserInfo(googleAccessToken);
        log.info("User information from Google: email={}, name={}", userInfo.getEmail(), userInfo.getNickname());

        // 이메일로 기존 사용자 조회
        Optional<User> existingUser = userRepository.findUserByEmail(userInfo.getEmail());

        if (existingUser.isPresent()) {
            // 기존 사용자일 경우 JWT 액세스 및 리프레시 토큰을 한 번 생성하여 사용
            TokenInfo tokenInfo = jwtProvider.generateToken(existingUser.get());
            String accessToken = tokenInfo.getAccessToken();
            String refreshToken = tokenInfo.getRefreshToken();
            log.info("Existing user found. Tokens issued: AccessToken={}, RefreshToken={}", accessToken, refreshToken);

            // 두 토큰 모두 저장
            saveJwtToken(existingUser.get(), accessToken, refreshToken);

            // TokenResponseDTO 반환
            return new TokenResponseDTO(accessToken, refreshToken);
        } else {
            log.info("No existing user found. Proceeding with signup.");

            // 새로운 사용자 생성
            AuthRequestDTO authRequestDTO = AuthRequestDTO.builder()
                    .email(userInfo.getEmail())
                    .nickname(userInfo.getNickname())
                    .profileImagePath(userInfo.getProfileImagePath())
                    .build();

            User newUser = userService.oauthSignup(authRequestDTO);
            log.info("User successfully signed up with Google: {}", newUser);

            // 새로운 사용자에 대해 JWT 액세스 및 리프레시 토큰을 한 번 생성하여 사용
            TokenInfo tokenInfo = jwtProvider.generateToken(newUser);
            String accessToken = tokenInfo.getAccessToken();
            String refreshToken = tokenInfo.getRefreshToken();
            log.info("New user found. Tokens issued: AccessToken={}, RefreshToken={}", accessToken, refreshToken);

            // 두 토큰 모두 저장
            saveJwtToken(newUser, accessToken, refreshToken);

            // TokenResponseDTO 반환
            return new TokenResponseDTO(accessToken, refreshToken);
        }
    }

    @Override
    // 리다이렉트 URL 생성 메소드
    public String generateLoginUrl() {
        String loginUrl = "https://accounts.google.com/o/oauth2/auth?client_id=" + googleClientId +
                "&redirect_uri=" + googleRedirectUri +
                "&response_type=code" +
                "&scope=openid%20email%20profile";
        log.info("Generated Google login URL: {}", loginUrl);
        return loginUrl;
    }

    // Google Access Token 요청
    private String getGoogleAccessToken(String code) {
        log.info("Requesting access token from Google for authorization code: {}", code);

        String tokenUrl = "https://oauth2.googleapis.com/token";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("code", code);

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

        AuthRequestDTO googleRequestDTO =
                AuthRequestDTO.builder()
                        .email(email)
                        .nickname(name)
                        .profileImagePath(profileImagePath)
                        .build();
        log.info("Google user information retrieved: email={}, name={}", googleRequestDTO.getEmail(), googleRequestDTO.getNickname());

        return googleRequestDTO;
    }

    // JWT 토큰을 MySQL에 저장하는 메소드
    private void saveJwtToken(User user, String accessToken, String refreshToken) {
        // 현재 사용자의 토큰이 이미 있는지 확인합니다.
        Optional<JwtToken> existingTokenOpt = jwtTokenRepository.findByUserId(user.getUserId());

        JwtToken jwtTokenEntity = existingTokenOpt.orElseGet(() ->
                JwtToken.builder()
                        .userId(user.getUserId())
                        .build()
        );

        // 토큰 값을 새로 설정
        jwtTokenEntity.setAccessToken(accessToken);
        jwtTokenEntity.setRefreshToken(refreshToken);
        jwtTokenEntity.setAccessExpirationTime(System.currentTimeMillis() + jwtProvider.getAccessTokenExpirationTime());
        jwtTokenEntity.setRefreshExpirationTime(System.currentTimeMillis() + jwtProvider.getRefreshTokenExpirationTime());

        // 갱신된 토큰을 저장합니다.
        jwtTokenRepository.save(jwtTokenEntity);
    }

}
