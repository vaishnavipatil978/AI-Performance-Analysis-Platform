package com.platform.user.presentation.controller;

import com.platform.common.dto.ApiResponse;
import com.platform.user.application.dto.request.LoginRequest;
import com.platform.user.application.dto.request.RefreshTokenRequest;
import com.platform.user.application.dto.request.RegisterRequest;
import com.platform.user.application.dto.response.AuthResponse;
import com.platform.user.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, and token management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("User registered successfully", authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and obtain tokens")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Token refreshed", authService.refreshToken(request.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke refresh token")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.success("Logout successful", null);
    }
}
