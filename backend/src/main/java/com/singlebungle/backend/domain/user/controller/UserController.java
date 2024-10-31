package com.singlebungle.backend.domain.user.controller;

import com.singlebungle.backend.domain.user.service.UserService;
import com.singlebungle.backend.global.exception.model.NoTokenRequestException;
import com.singlebungle.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "로그아웃", description = "로그아웃을 처리하고 토큰을 삭제합니다.")
    @GetMapping("/logout")
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
