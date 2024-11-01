package com.singlebungle.backend.global.auth.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends GenericFilterBean { // JWT 토큰을 추출하고, 이를 검증하여 인증 정보를 설정한다.
    // JWT 토큰의 생성 및 검증을 담당하는 클래스다.
    private final JwtProvider jwtProvider;

    // JWT 검증을 건너뛰어야 하는 URI와 HTTP 메소드 목록
    private static final Map<String, Set<String>> EXCLUDE_URLS = new HashMap<>() {{
        put("/api/oauth2/code/google", new HashSet<>(List.of("GET")));
        put("/api/oauth2/google/authorize", new HashSet<>(List.of("GET")));
        put("/api/auth/logout", new HashSet<>(List.of("POST"))); // 로그아웃 제외
        put("/api/user/oauth2/", new HashSet<>(List.of("POST"))); // 소셜 로그인 리다이렉션 제외
        put("/api/error", new HashSet<>(List.of("GET", "POST"))); // error 제외
        put("/api/swagger-ui", new HashSet<>(List.of("GET"))); // swagger 제외
        put("/api/v3/api-docs", new HashSet<>(List.of("GET"))); // swagger 제외
        put("/api/oauth2", new HashSet<>(List.of("GET", "POST"))); // oauth 제외
    }};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // 요청 url 로그
        log.info("Incoming request to URL: {}, Method: {}", requestURI, method);

        // 특정 경로는 토큰 검사를 하지 않음
        if (isExcludedUrl(requestURI, method)) {
            log.info("Request URL {} and Method {} is excluded from JWT validation", requestURI, method);
            chain.doFilter(request, response); // 검증을 건너뜀
            return;
        }

        // Request Header 에서 JWT 추출
        String token = resolveToken(httpRequest);

        // 토큰 유효성 검사
        if (token != null && jwtProvider.validateToken(token)) {
            // 토큰을 확인
            String Token = jwtProvider.getTokenFromDatabase(jwtProvider.getUserIdFromToken(token), token);
            if (Token != null && Token.equals(token)) {
                // 토큰이 유효할 경우, 토큰에서 Authentication 객체를 가져와 SecurityContext에 저장
                Authentication authentication = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Authenticated user with token");

                // JWT가 유효하다면 Authorization 헤더에서 Bearer 없이 변경한 값을 설정
                HttpServletRequestWrapper modifiedRequest = new HttpServletRequestWrapper(httpRequest) {
                    @Override
                    public String getHeader(String name) {
                        if ("Authorization".equals(name)) {
                            // Bearer 접두어 없이 토큰을 반환
                            return token;
                        }
                        return super.getHeader(name);
                    }
                };

                chain.doFilter(request, response); // 필터 체인의 다음 필터를 호출하여 요청을 처리한다.
                return;
            } else {
                log.error("Token is either missing or not valid in DB");
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token in DB");
                return;
            }
        } else {
            // 토큰이 없거나 유효하지 않을 경우 401 에러 반환
            log.error("JWT token is missing or invalid");
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token is missing or invalid");
            return;
        }

    }

    // 검증을 건너뛰어야 하는 URL과 메소드를 확인하는 메소드
    private boolean isExcludedUrl(String requestURI, String method) {
        return EXCLUDE_URLS.entrySet().stream()
                .anyMatch(entry -> requestURI.startsWith(entry.getKey()) && entry.getValue().contains(method));
    }


    /**
     * Request Header 에서 토큰 정보를 추출하는 함수
     */
    private String resolveToken(HttpServletRequest request) {
        // request.getHeader("Authorization") 요청 헤더에서 Authorization 헤더의 값을 가져온다. 이 헤더는 일반적으로 "Bearer {JWT}" 형식으로 JWT를 포함한다.
        String bearerToken = request.getHeader("Authorization");
        // hasText로 bearerToken이 비어 있지 않은지 확인 && bearerToken이 "Bearer "로 시작하는지 확인한다.
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            // "Bearer " 문자열 이후의 부분을 반환합니다. 즉, 실제 JWT를 반환한다.
            return bearerToken.substring(7);
        }
        return null;
    }
}
