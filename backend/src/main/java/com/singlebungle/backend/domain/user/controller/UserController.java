package com.singlebungle.backend.domain.user.controller;

import com.singlebungle.backend.domain.user.dto.response.UserInfoResponseDTO;
import com.singlebungle.backend.domain.user.service.UserService;
import com.singlebungle.backend.global.auth.TokenInfo;
import com.singlebungle.backend.global.auth.auth.JwtProvider;
import com.singlebungle.backend.global.auth.dto.TokenResponseDTO;
import com.singlebungle.backend.global.exception.model.NoTokenRequestException;
import com.singlebungle.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "유저 정보 조회", description = "액세스 토큰을 통해 유저 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 정보 조회 성공"),
            @ApiResponse(responseCode = "400", description = "토큰이 유효하지 않음")
    })
    @GetMapping
    public ResponseEntity<UserInfoResponseDTO> getUserInfo(
            @Parameter(description = "Authorization 헤더에 포함된 액세스 토큰")
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @PostMapping("/logout")
    public ResponseEntity<BaseResponseBody> logout(
            @Parameter(description = "Authorization 헤더에 포함된 액세스 토큰")
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

    @Operation(summary = "액세스 토큰 재발급", description = "쿠키에 저장된 리프레시 토큰을 사용하여 새로운 액세스 토큰을 재발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "액세스 토큰 재발급 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 리프레시 토큰")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshAccessToken(
            @Parameter(description = "쿠키에 저장된 리프레시 토큰")
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        // 리프레시 토큰이 없으면 예외 처리
        if (refreshToken == null) {
            throw new NoTokenRequestException("리프레시 토큰이 필요합니다.");
        }

        // 사용자 ID를 가져오기 위한 리프레시 토큰의 유효성 검증 및 사용자 정보 추출
        Long userId = jwtProvider.getUserIdFromToken(refreshToken);

        // 액세스 토큰 재발급
        TokenInfo tokenInfo = jwtProvider.regenerateAccessToken(userId, refreshToken);

        // "accessToken"을 키로 하는 Map을 생성하여 반환
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", tokenInfo.getAccessToken());

        return ResponseEntity.ok(response);
    }

}
