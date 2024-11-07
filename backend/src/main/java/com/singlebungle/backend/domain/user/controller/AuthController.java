package com.singlebungle.backend.domain.user.controller;

import com.singlebungle.backend.domain.user.service.GoogleAuthService;
import com.singlebungle.backend.global.auth.dto.TokenResponseDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final GoogleAuthService googleAuthService;
    // Google 인증 리다이렉트 URL (크롬 익스텐션)
    private static final String EXTENSION_REDIRECT_URI = "https://fcnagokaapcdhckhnndheebebadioomb.chromiumapp.org/google";
    // 로컬 개발 환경 리다이렉트 URL
    private static final String LOCAL_REDIRECT_URI = "https://k11b205.p.ssafy.io/login-callback";


    @GetMapping("/google/authorize")
    public ResponseEntity<?> getGoogleAuthorizeUrl(@RequestParam(required = false) String platform) {
        log.info("Generating Google login URL");

        String redirectUri;

        // 요청 파라미터로 전달된 platform을 사용
        if ("extension".equals(platform)) {
            redirectUri = EXTENSION_REDIRECT_URI;
        } else {
            redirectUri = LOCAL_REDIRECT_URI;
        }

        // Redirect URI를 포함하여 Google 로그인 URL 생성
        String googleAuthorizeUrl = googleAuthService.generateLoginUrl(redirectUri);
        log.info("Generated Google login URL: {}", googleAuthorizeUrl);

        return ResponseEntity.ok().body(googleAuthorizeUrl);
    }

    @GetMapping("/code/google")
    public ResponseEntity<?> googleCallback(@RequestParam String code, @RequestParam(required = false) String platform, HttpServletResponse response) {
        try {
            log.info("Received Google authorization code: {}", code);

            String redirectUri;
            if ("extension".equals(platform)) {
                redirectUri = EXTENSION_REDIRECT_URI;
            } else {
                redirectUri = LOCAL_REDIRECT_URI;
            }

            log.info("Redirect URI selected: {}", redirectUri);

            // 동적으로 전달된 redirectUri를 사용하여 처리
            TokenResponseDTO tokenResponse = googleAuthService.handleLoginOrSignup(code, redirectUri);

            if (tokenResponse != null) {
                log.info("Google login or signup successful, tokens issued.");

                // 리프레시 토큰을 쿠키에 저장
                Cookie refreshTokenCookie = new Cookie("refreshToken", tokenResponse.getRefreshToken());
                refreshTokenCookie.setHttpOnly(true); // JavaScript에서 접근 불가
                refreshTokenCookie.setPath("/"); // 쿠키의 유효 경로 설정
                refreshTokenCookie.setMaxAge(60 * 60 * 24 * 30); // 30일 동안 유효
                response.addCookie(refreshTokenCookie); // 쿠키를 응답에 추가

                // 액세스 토큰을 응답 바디에 담아 반환
                return ResponseEntity.ok()
                        .body(Collections.singletonMap("access-token", tokenResponse.getAccessToken()));
            } else {
                log.error("Token response is null, something went wrong.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Something went wrong while processing tokens.");
            }
        } catch (Exception e) {
            log.error("Google OAuth2 authentication failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OAuth2 authentication failed");
        }
    }

//    @GetMapping("/code/google")
//    public ResponseEntity<?> googleCallback(@RequestParam String code, @RequestParam(required = false) String platform, HttpServletResponse response) {
//        try {
//            log.info("Received Google authorization code: {}", code);
//
//            String redirectUri;
//            if ("extension".equals(platform)) {
//                redirectUri = EXTENSION_REDIRECT_URI;
//            } else {
//                redirectUri = LOCAL_REDIRECT_URI;
//            }
//
//            log.info("Redirect URI selected: {}", redirectUri);
//
//            // 동적으로 전달된 redirectUri를 사용하여 처리
//            TokenResponseDTO tokenResponse = googleAuthService.handleLoginOrSignup(code, redirectUri);
//
//            if (tokenResponse != null) {
//                log.info("Google login or signup successful, tokens issued.");
//
//                // 리프레시 토큰을 쿠키에 저장
//                Cookie refreshTokenCookie = new Cookie("refreshToken", tokenResponse.getRefreshToken());
//                refreshTokenCookie.setHttpOnly(true); // JavaScript에서 접근 불가
//                refreshTokenCookie.setPath("/"); // 쿠키의 유효 경로 설정
//                refreshTokenCookie.setMaxAge(60 * 60 * 24 * 30); // 30일 동안 유효
//                response.addCookie(refreshTokenCookie); // 쿠키를 응답에 추가
//
//                // 액세스 토큰을 쿼리 파라미터로 반환
//                return ResponseEntity.status(HttpStatus.FOUND)
//                        .header("Location", "https://k11b205.p.ssafy.io/?accessToken=" + tokenResponse.getAccessToken())
//                        .build();
//            } else {
//                log.error("Token response is null, something went wrong.");
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body("Something went wrong while processing tokens.");
//            }
//        } catch (Exception e) {
//            log.error("Google OAuth2 authentication failed", e);
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OAuth2 authentication failed");
//        }
//    }
}