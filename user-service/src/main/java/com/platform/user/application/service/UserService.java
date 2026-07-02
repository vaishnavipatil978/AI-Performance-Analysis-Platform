package com.platform.user.application.service;

import com.platform.common.dto.PageResponse;
import com.platform.user.application.dto.request.UpdateProfileRequest;
import com.platform.user.application.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse getUserById(UUID id);

    UserResponse getUserByEmail(String email);

    UserResponse updateProfile(UUID id, UpdateProfileRequest request);

    PageResponse<UserResponse> getAllUsers(int page, int size);

    void deactivateUser(UUID id);
}
