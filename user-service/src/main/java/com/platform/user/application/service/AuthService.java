package com.platform.user.application.service;

import com.platform.user.application.dto.request.LoginRequest;
import com.platform.user.application.dto.request.RegisterRequest;
import com.platform.user.application.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);
}
