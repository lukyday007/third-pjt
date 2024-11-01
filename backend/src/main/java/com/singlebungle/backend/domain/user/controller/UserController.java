package com.singlebungle.backend.domain.user.controller;

import com.singlebungle.backend.domain.user.dto.response.UserInfoResponseDTO;
import com.singlebungle.backend.domain.user.service.UserService;
import com.singlebungle.backend.global.auth.auth.JwtProvider;
import com.singlebungle.backend.global.exception.model.NoTokenRequestException;
import com.singlebungle.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "유저 정보 조회", description = "액세스 토큰을 통해 유저 정보를 반환합니다.")
    @GetMapping
    public ResponseEntity<UserInfoResponseDTO> getUserInfo(
            @RequestHeader(value = "Authorization", required = false) String token) {

        if (token == null) {
            throw new NoTokenRequestException("Access 토큰이 필요합니다.");
        }
        log.info(">>> [GET] /users/info - 유저 정보 요청: {}", token);

        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info(">>> [GET] /users/info - Bearer 제거 후 토큰: {}", token);
        }

        // Extract user ID from the token
        Long userId = jwtProvider.getUserIdFromToken(token);
        log.info(">>> [GET] /users/info - Token으로부터 추출된 userID: {}", userId);

        // Fetch user information and convert it to UserInfoResponseDTO
        UserInfoResponseDTO userInfo = userService.getUserInfoById(userId);
        log.info(">>> [GET] /users/info - 유저 정보 반환: {}", userInfo);

        return ResponseEntity.ok(userInfo);
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 처리하고 토큰을 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<BaseResponseBody> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null)
            throw new NoTokenRequestException("Access 토큰이 필요합니다.");
        log.info(">>> [GET] /user/logout - 로그아웃 요청: {}", token);
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info(">>> [GET] /user/logout - Bearer 제거 후 토큰: {}", token);
        }
        userService.userSignOut(token);
        log.info(">>> [GET] /user/logout - 로그아웃 완료");
        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "로그아웃이 완료되었습니다."));
    }

}
