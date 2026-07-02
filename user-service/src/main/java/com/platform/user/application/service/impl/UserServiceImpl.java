package com.platform.user.application.service.impl;

import com.platform.common.dto.PageResponse;
import com.platform.user.application.dto.request.UpdateProfileRequest;
import com.platform.user.application.dto.response.UserResponse;
import com.platform.user.application.mapper.UserMapper;
import com.platform.user.application.service.UserService;
import com.platform.user.domain.exception.UserNotFoundException;
import com.platform.user.domain.model.User;
import com.platform.user.domain.repository.UserRepository;
import com.platform.user.infrastructure.cache.UserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserCacheService cacheService;

    @Override
    public UserResponse getUserById(UUID id) {
        return cacheService.get(id).orElseGet(() -> {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));
            UserResponse response = userMapper.toResponse(user);
            cacheService.cache(id, response);
            return response;
        });
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        User updated = userRepository.save(user);
        cacheService.evict(id);
        log.info("Profile updated for user: {}", id);
        return userMapper.toResponse(updated);
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        Page<User> users = userRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.from(users.map(userMapper::toResponse));
    }

    @Override
    @Transactional
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);
        userRepository.save(user);
        cacheService.evict(id);
        log.info("User deactivated: {}", id);
    }
}
