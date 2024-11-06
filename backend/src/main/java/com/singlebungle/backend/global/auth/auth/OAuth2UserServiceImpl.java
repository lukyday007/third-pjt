package com.singlebungle.backend.global.auth.auth;

import com.singlebungle.backend.domain.user.dto.request.AuthRequestDTO;
import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.domain.user.repository.UserRepository;
import com.singlebungle.backend.domain.user.service.UserService;
import com.singlebungle.backend.global.auth.TokenInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        log.info("OAuth2UserRequest received: {}", userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2User loaded: {}", oAuth2User);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.info("User attributes received: {}", attributes);

        AuthRequestDTO authRequestDTO = null;
        if ("google".equals(registrationId)) {
            authRequestDTO = getGoogleUser(attributes);
        }

        // 사용자 정보를 UserService로 전달해 새 사용자 등록 또는 기존 사용자 확인
        User userInfoResponseDTO = userService.oauthSignup(authRequestDTO);

        if (userInfoResponseDTO != null) { // 기존 사용자
            // 기존 사용자 로그인 성공 시 JWT 토큰 발급
            User user = userRepository.findUserByEmail(userInfoResponseDTO.getEmail())
                    .orElseThrow(RuntimeException::new);
            TokenInfo tokenInfo = jwtProvider.generateToken(user);

            // JWT 토큰을 헤더에 포함하여 반환
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            if (response != null) {
                response.setHeader("Authorization", "Bearer " + tokenInfo.getAccessToken());
            }
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }

    private AuthRequestDTO getGoogleUser(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        log.info("Google user details: email={}, name={}", email, name);

        return AuthRequestDTO.builder()
                .email(email)
                .nickname(name)
                .build();
    }
}