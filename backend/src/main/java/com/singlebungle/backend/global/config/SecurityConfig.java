package com.singlebungle.backend.global.config;

import com.singlebungle.backend.global.auth.auth.JwtAuthenticationEntryPoint;
import com.singlebungle.backend.global.auth.auth.JwtAuthenticationFilter;
import com.singlebungle.backend.global.auth.auth.JwtProvider;
import com.singlebungle.backend.global.auth.auth.OAuth2UserServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration // 이 클래스는 Spring의 설정 클래스로 사용
@EnableWebSecurity
// Spring Security를 활성화합니다. 이 어노테이션은 웹 보안 기능을 추가하고, 기본 보안 설정을 적용
@RequiredArgsConstructor
// final로 선언된 필드에 대한 생성자를 자동으로 생성. JwtProvider 주입을 위해 사용
public class SecurityConfig {
    // 이 친구는 JWT 토큰의 생성 및 검증을 담당
    private final JwtProvider jwtProvider;
    private final OAuth2UserServiceImpl oAuth2UserService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    // 이 메소드가 반환하는 객체를 빈으로 등록. filterChain 메소드는 Spring Security의 주요 설정을 구성한다. HttpSecurity 객체로 다양한 보안 설정이 가능하다.
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer -> { // 세션 관리 설정이다.
                    // 세션을 사용하지 않도록 설정한다. 모든 요청은 상태 없이 처리되며, JWT를 통해 인증을 관리하도록 설정한다.
                    httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> { // HTTP 요청에 대한 접근 권한을 설정한다.
                    authorizationManagerRequestMatcherRegistry
                            .requestMatchers("/**").permitAll()
//                            .requestMatchers("/oauth2/**, /users/refresh-token").permitAll()
                            .anyRequest().authenticated(); // 나머지 모든 요청에 대해 인증을 요구
                })
                .headers(httpSecurityHeadersConfigurer ->
                        httpSecurityHeadersConfigurer.xssProtection(HeadersConfigurer.XXssConfig::disable)) // XSS 보호 설정 추가
                // UsernamePasswordAuthenticationFilter(사용자 이름과 비밀번호를 통한 기본 인증 필터) 전에 JWT 인증을 처리하는 JwtAuthenticationFilter((user-defined) 필터 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                // 직접 정의한 CorsFilter 추가
                .addFilterBefore(corsFilter(), JwtAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler((request, response, accessDeniedException) ->  // accessDeniedHandler 추가
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                );

        return http.build();
    }

    // 추가된 CORS 설정
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("https://k11b205.p.ssafy.io");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedOrigin("https://plogbucket.s3.ap-northeast-2.amazonaws.com/**");
        config.addAllowedOriginPattern("https://plogbucket.s3.ap-northeast-2.amazonaws.com/**");
        config.addAllowedOriginPattern("http://localhost:3000");
        config.addAllowedOriginPattern("http://localhost:8080");
        config.addAllowedOriginPattern("https://k11b205.p.ssafy.io");
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}