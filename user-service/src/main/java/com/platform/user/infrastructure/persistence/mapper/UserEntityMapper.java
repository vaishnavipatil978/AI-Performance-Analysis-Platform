package com.platform.user.infrastructure.persistence.mapper;

import com.platform.user.domain.model.User;
import com.platform.user.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    UserEntity toEntity(User domain);

    User toDomain(UserEntity entity);
}
