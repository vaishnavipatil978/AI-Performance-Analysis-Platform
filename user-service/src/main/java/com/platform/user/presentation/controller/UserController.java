package com.platform.user.presentation.controller;

import com.platform.common.dto.ApiResponse;
import com.platform.common.dto.PageResponse;
import com.platform.user.application.dto.request.UpdateProfileRequest;
import com.platform.user.application.dto.response.UserResponse;
import com.platform.user.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ApiResponse<UserResponse> getMe(@RequestHeader("X-User-ID") UUID userId) {
        return ApiResponse.success("Profile retrieved", userService.getUserById(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ApiResponse<UserResponse> updateMe(@RequestHeader("X-User-ID") UUID userId,
                                              @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("Profile updated", userService.updateProfile(userId, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (Admin only)")
    public ApiResponse<UserResponse> getUserById(@PathVariable UUID id) {
        return ApiResponse.success("User retrieved", userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users (Admin only)")
    public ApiResponse<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("Users retrieved", userService.getAllUsers(page, size));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate user (Admin only)")
    public void deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
    }
}
