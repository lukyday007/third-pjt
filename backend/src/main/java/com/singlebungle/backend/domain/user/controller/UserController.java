package com.singlebungle.backend.domain.user.controller;

import com.singlebungle.backend.domain.user.dto.response.UserInfoResponseDTO;
import com.singlebungle.backend.domain.user.service.UserService;
import com.singlebungle.backend.global.auth.TokenInfo;
import com.singlebungle.backend.global.auth.auth.JwtProvider;
import com.singlebungle.backend.global.exception.model.NoTokenRequestException;
import com.singlebungle.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletResponse response) {
        if (token == null)
            throw new NoTokenRequestException("Access 토큰이 필요합니다.");
        log.info(">>> [GET] /user/logout - 로그아웃 요청: {}", token);
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info(">>> [GET] /user/logout - Bearer 제거 후 토큰: {}", token);
        }
        userService.userSignOut(token);
        log.info(">>> [GET] /user/logout - 로그아웃 완료");

        // 쿠키에서 리프레시 토큰 삭제
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setPath("/"); // 쿠키가 적용된 경로
        refreshTokenCookie.setMaxAge(0); // 만료 시간 0으로 설정하여 즉시 삭제
        refreshTokenCookie.setHttpOnly(true); // HttpOnly 설정 유지
        response.addCookie(refreshTokenCookie); // 응답에 쿠키 추가하여 삭제

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


    @Operation(summary = "회원 탈퇴", description = "회원의 상태를 DELETED로 변경하여 탈퇴를 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @DeleteMapping
    public ResponseEntity<BaseResponseBody> deleteAccount(
            @Parameter(description = "Authorization 헤더에 포함된 액세스 토큰")
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletResponse response) {

        if (token == null) {
            throw new NoTokenRequestException("Access 토큰이 필요합니다.");
        }

        log.info(">>> [DELETE] /user/delete - 회원 탈퇴 요청: {}", token);

        // Bearer 토큰 형식 제거
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info(">>> [DELETE] /user/delete - Bearer 제거 후 토큰: {}", token);
        }

        // 토큰을 통해 사용자 확인 및 상태 변경
        userService.deleteUser(token); // 이 메서드가 상태를 `DELETED`로 변경하도록 수정되어야 합니다.
        log.info(">>> [DELETE] /user/delete - 회원 탈퇴 완료");

        // 리프레시 토큰 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "회원 탈퇴가 완료되었습니다."));
    }
}
