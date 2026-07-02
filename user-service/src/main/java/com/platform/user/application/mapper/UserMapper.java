package com.platform.user.application.mapper;

import com.platform.user.application.dto.response.UserResponse;
import com.platform.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserResponse toResponse(User user);
}
