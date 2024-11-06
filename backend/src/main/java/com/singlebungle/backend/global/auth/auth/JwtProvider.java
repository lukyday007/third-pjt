package com.singlebungle.backend.global.auth.auth;

import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.global.auth.TokenInfo;
import com.singlebungle.backend.global.auth.entity.JwtToken;
import com.singlebungle.backend.global.auth.repository.JwtTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final Key key; // JWT 서명을 위한 키
    private final JwtTokenRepository jwtTokenRepository; // JWT 토큰 리포지토리
    private final long TOKEN_EXPIRATION_TIME = 3600 * 24; // 토큰 만료 시간 설정 (예: 1일)
    // 리프레시 토큰 만료 시간 설정 (30일)
    private final long REFRESH_TOKEN_EXPIRATION_TIME = 3600 * 24 * 30 * 1000L;

    public JwtProvider(@Value("${jwt.secretkey}") String secretKey, JwtTokenRepository jwtTokenRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtTokenRepository = jwtTokenRepository;
    }

    // 새로운 액세스 토큰과 리프레시 토큰을 생성하고 DB에 저장하는 메소드
    @Transactional
    public TokenInfo generateAndSaveToken(User user, String redirectUrl) {
        // 액세스 토큰과 리프레시 토큰의 만료 시간 설정
        Date accessTokenExpiresIn = new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME * 1000);
        Date refreshTokenExpiresIn = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME * 1000);

        // 액세스 토큰 생성
        String accessToken = Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 리프레시 토큰 생성
        String refreshToken = Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 새로운 JWT 토큰을 DB에 저장
        JwtToken jwtToken = JwtToken.builder()
                .userId(user.getUserId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpirationTime(accessTokenExpiresIn.getTime())
                .refreshExpirationTime(refreshTokenExpiresIn.getTime())
                .redirectUri(redirectUrl)
                .build();
        jwtTokenRepository.save(jwtToken); // MySQL에 저장

        // 발급된 토큰 정보 반환
        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenInfo generateToken(User user) {
        // 액세스 토큰과 리프레시 토큰의 만료 시간 설정
        Date accessTokenExpiresIn = new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME * 1000);
        Date refreshTokenExpiresIn = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME * 1000); // 리프레시 토큰은 더 긴 만료 시간

        // 액세스 토큰 생성
        String accessToken = Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 리프레시 토큰 생성
        String refreshToken = Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .setExpiration(refreshTokenExpiresIn)  // Refresh Token의 만료 시간 설정
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 기존 토큰 삭제
//        jwtTokenRepository.deleteByUserId(user.getUserId());

        // 새로운 JWT 토큰을 DB에 저장
        JwtToken jwtToken = JwtToken.builder()
                .userId(user.getUserId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpirationTime(accessTokenExpiresIn.getTime()) // Access Token 만료 시간 저장
                .refreshExpirationTime(refreshTokenExpiresIn.getTime()) // Refresh Token 만료 시간 저장
                .build();
        jwtTokenRepository.save(jwtToken); // MySQL에 저장

        // 발급된 토큰 정보 반환
        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 사용자의 정보를 포함한 JWT 토큰 생성 메소드
    @Transactional
    public TokenInfo generateToken(User user, String redirectUrl) {
        // 액세스 토큰과 리프레시 토큰의 만료 시간 설정
        Date accessTokenExpiresIn = new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME * 1000);
        Date refreshTokenExpiresIn = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME * 1000); // 리프레시 토큰은 더 긴 만료 시간

        // 액세스 토큰 생성
        String accessToken = Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 리프레시 토큰 생성
        String refreshToken = Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .setExpiration(refreshTokenExpiresIn)  // Refresh Token의 만료 시간 설정
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

//        // 기존 토큰 삭제
//        jwtTokenRepository.deleteByUserId(user.getUserId());

        // 새로운 JWT 토큰을 DB에 저장
        JwtToken jwtToken = JwtToken.builder()
                .userId(user.getUserId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpirationTime(accessTokenExpiresIn.getTime()) // Access Token 만료 시간 저장
                .refreshExpirationTime(refreshTokenExpiresIn.getTime()) // Refresh Token 만료 시간 저장
                .redirectUri(redirectUrl)
                .build();
        jwtTokenRepository.save(jwtToken); // MySQL에 저장

        // 발급된 토큰 정보 반환
        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // JWT 토큰을 파싱하여 인증 정보를 가져오는 메소드
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken); // JWT를 파싱하여 클레임 정보를 가져온다.
        Collection<? extends GrantedAuthority> authorities = new ArrayList<>(); // 사용자 권한을 저장할 컬렉션이다.

        // UserDetails 객체를 생성한다. 사용자의 이름은 클레임의 주체(Subject)에서 가져오고, 비밀번호와 권한은 빈 값으로 설정한다.
        UserDetails principal = new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities); // 인증 객체 반환
    }

    // JWT 토큰의 유효성을 검증하는 메소드
    public boolean validateToken(String token) {
        try {
            // JWT를 파싱하여 유효성을 검증한다. 서명을 확인하고, 클레임을 파싱한다.
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            log.info("validateToken : " + token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException |
                 MalformedJwtException e) { // 잘못된 JWT 서명인 경우
            log.error("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) { // 만료된 JWT인 경우
            log.error("Expired JWT Token");
        } catch (UnsupportedJwtException e) { // 지원되지 않는 JWT인 경우
            log.error("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) { // JWT 클레임 문자열이 비어있는 경우
            log.error("JWT claims string is empty.", e);
        } catch (Exception e) {
            log.error("validateToken에서 일어난 그 외 무언가 에러");
        }
        return false;
    }

    // JWT 토큰의 클레임을 파싱하는 메소드
    private Claims parseClaims(String accessToken) {
        try {
            // JWT를 파싱하여 클레임(Claims) 정보를 추출한다.
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) { // 토큰이 만료된 경우
            return e.getClaims();
        }
    }

    // DB에서 JWT 토큰을 조회하는 메소드
    public String getTokenFromDatabase(Long userId, String token) {
        return jwtTokenRepository.findByUserIdAndAccessToken(userId, token)
                .map(JwtToken::getAccessToken)
                .orElse(null); // 해당 사용자 ID의 토큰을 조회하여 반환
    }

    // JWT 토큰에서 userId를 추출하는 메소드
    public Long getUserIdFromToken(String token) {
        String resolvedToken = resolveToken(token); // Bearer 접두어가 있는 경우 접두어 제거
        Claims claims = parseClaims(resolvedToken); // JWT의 클레임을 파싱
        return Long.parseLong(claims.getSubject()); // 토큰의 주체(Subject)인 userId를 반환
    }

    // Bearer 접두어를 확인하고 제거하는 메소드
    private String resolveToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7); // "Bearer "를 제거
        }
        return token; // Bearer 접두어가 없는 경우, 그대로 반환
    }

    // 예시: jwtProvider에 추가된 만료 시간 메서드
    public long getAccessTokenExpirationTime() {
        return TOKEN_EXPIRATION_TIME * 1000;
    }

    public long getRefreshTokenExpirationTime() {
        return REFRESH_TOKEN_EXPIRATION_TIME * 1000;
    }

    // 새로운 액세스 토큰만 생성하고 저장하는 메소드
    public TokenInfo regenerateAccessToken(Long userId, String refreshToken) {
        // 리프레시 토큰을 통해 기존 토큰을 조회
        JwtToken existingToken = jwtTokenRepository.findByUserIdAndRefreshToken(userId, refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("User not found or no token available"));

        // 리프레시 토큰이 유효한지 확인
        if (!validateToken(existingToken.getRefreshToken())) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // 새로운 액세스 토큰 생성
        Date newAccessTokenExpiresIn = new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME * 1000);
        String newAccessToken = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(newAccessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 기존 JWT 토큰의 액세스 토큰과 만료 시간만 업데이트
        existingToken.setAccessToken(newAccessToken);
        existingToken.setAccessExpirationTime(newAccessTokenExpiresIn.getTime());
        jwtTokenRepository.save(existingToken); // MySQL에 업데이트

        // 새로 발급한 액세스 토큰 반환
        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(newAccessToken)
                .refreshToken(existingToken.getRefreshToken()) // 기존 리프레시 토큰 반환
                .build();
    }
}