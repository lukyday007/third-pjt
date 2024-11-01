package com.singlebungle.backend.domain.user.service;

import com.singlebungle.backend.global.auth.dto.TokenResponseDTO;

public interface AuthService {
    final int EXPIRATION_TIME = 3600 * 24;
    TokenResponseDTO handleLoginOrSignup(String code, String redirect_url);

    String generateLoginUrl(String redirectUri);
}
