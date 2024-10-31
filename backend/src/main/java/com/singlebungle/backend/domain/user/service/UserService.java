package com.singlebungle.backend.domain.user.service;

import com.singlebungle.backend.domain.user.dto.request.AuthRequestDTO;
import com.singlebungle.backend.domain.user.dto.request.UserInfoRequestDTO;
import com.singlebungle.backend.domain.user.dto.response.UserInfoResponseDTO;
import com.singlebungle.backend.domain.user.entity.User;

public interface UserService {
    void save(UserInfoRequestDTO requestDTO);

    UserInfoResponseDTO oauthLogin(AuthRequestDTO authRequestDTO);

    User oauthSignup(AuthRequestDTO authRequestDTO);

    void userSignOut(String token);
}