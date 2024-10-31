package com.singlebungle.backend.domain.user.controller;

import com.singlebungle.backend.domain.user.service.GoogleAuthService;
import com.singlebungle.backend.global.auth.dto.TokenResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Slf4j // 로그를 사용하기 위해 추가
public class AuthController {

    private final GoogleAuthService googleAuthService;

    // Google OAuth2 로그인 리다이렉트 URL 제공
    @GetMapping("/google/authorize")
    public ResponseEntity<?> getGoogleAuthorizeUrl() {
        log.info("Generating Google login URL");
        String googleAuthorizeUrl = googleAuthService.generateLoginUrl();
        log.info("Google login URL: {}", googleAuthorizeUrl);
        return ResponseEntity.ok().body(googleAuthorizeUrl);
    }

    // Google 로그인 콜백 처리
    @GetMapping("/code/google")
    public ResponseEntity<?> googleCallback(@RequestParam String code) {
        try {
            log.info("Received Google authorization code: {}", code);
            TokenResponseDTO tokenResponse = googleAuthService.handleLoginOrSignup(code);

            // 토큰을 발급 받은 경우
            if (tokenResponse != null) {
                log.info("Google login or signup successful, tokens issued.");
                return ResponseEntity.ok(tokenResponse);
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
}
