package com.singlebungle.backend.domain.user.service;

import com.singlebungle.backend.domain.user.dto.request.AuthRequestDTO;
import com.singlebungle.backend.domain.user.dto.request.UserInfoRequestDTO;
import com.singlebungle.backend.domain.user.dto.response.UserInfoResponseDTO;
import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.domain.user.repository.UserRepository;
import com.singlebungle.backend.global.auth.auth.JwtProvider;
import com.singlebungle.backend.global.auth.service.JwtTokenService;
import com.singlebungle.backend.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final JwtTokenService jwtTokenService;

    @Override
    public void save(UserInfoRequestDTO requestDTO) {
        User user = User.convertToEntity(requestDTO);
        userRepository.save(user);
    }

    @Override
    public UserInfoResponseDTO oauthLogin(AuthRequestDTO authRequestDTO) {
        try {
            // 이메일로 기존 사용자 검색
            Optional<User> existingUser = userRepository.findUserByEmail(authRequestDTO.getEmail());

            if (existingUser.isPresent()) {
                log.info("기존 사용자 로그인: {}", existingUser.get().getEmail());
                return UserInfoResponseDTO.convertToDTO(existingUser.get());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
            }
//          else {
//                // 새로운 사용자 등록
//                User user = User.builder()
//                        .email(authRequestDTO.getEmail())
//                        .nickname(authRequestDTO.getNickname())
//                        .build();
//                log.info("새로운 사용자 등록: {}", authRequestDTO.getEmail());
//                userRepository.save(user);
//                return UserInfoResponseDTO.convertToDTO(user);
//            }
        } catch (Exception e) {
            log.error("사용자 처리 중 예외 발생: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 처리 중 문제가 발생했습니다.");
        }
    }

    public User oauthSignup(AuthRequestDTO authRequestDTO) {
        // 유효성 검사
        if (authRequestDTO.getEmail() == null || authRequestDTO.getNickname() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일과 닉네임은 필수입니다.");
        }

        // 새로운 사용자 등록
        User user = User.builder()
                .email(authRequestDTO.getEmail())
                .nickname(authRequestDTO.getNickname())
                .profileImagePath(authRequestDTO.getProfileImagePath())
                .status(User.Status.ACTIVE)
                .build();

        log.info("새로운 사용자 등록: {}", authRequestDTO.getEmail());
        return userRepository.save(user); // User 엔티티를 반환
    }

    @Override
    public void userSignOut(String token) {
        log.info(">>> [USER SIGN OUT] - 사용자 로그아웃 요청: 토큰 = {}", token);

        // 토큰 삭제
        jwtTokenService.deleteToken(token);

        log.info(">>> [USER SIGN OUT] - 특정 토큰 삭제 완료: 토큰 = {}", token);

        SecurityContextHolder.clearContext();

        log.info(">>> [USER SIGN OUT] - SecurityContextHolder 초기화 완료");
    }


    public UserInfoResponseDTO getUserInfoById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID를 가진 유저를 찾을 수 없습니다: " + userId));

        return UserInfoResponseDTO.convertToDTO(user);
    }

    @Override
    public Long getUserByToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return jwtProvider.getUserIdFromToken(token);
    }

}