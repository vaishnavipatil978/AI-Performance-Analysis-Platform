package com.platform.user.infrastructure.persistence.mapper;

import com.platform.user.domain.model.RefreshToken;
import com.platform.user.infrastructure.persistence.entity.RefreshTokenEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RefreshTokenEntityMapper {

    RefreshTokenEntity toEntity(RefreshToken domain);

    RefreshToken toDomain(RefreshTokenEntity entity);
}
