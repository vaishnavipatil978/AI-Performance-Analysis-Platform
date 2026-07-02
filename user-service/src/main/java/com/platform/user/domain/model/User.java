package com.platform.user.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private Role role;
    private boolean active;
    private boolean emailVerified;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
