package com.singlebungle.backend.global.auth;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TokenInfo {
    private String grantType;
    private String accessToken;
    private String refreshToken;
}
