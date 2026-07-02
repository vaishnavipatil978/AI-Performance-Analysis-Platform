package com.platform.user.infrastructure.security;

import com.platform.user.domain.exception.UserNotFoundException;
import com.platform.user.domain.repository.UserRepository;
import com.platform.user.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userJpaRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        var entity = userJpaRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        return new User(
                entity.getId().toString(),
                entity.getPasswordHash(),
                entity.isActive(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + entity.getRole().name()))
        );
    }
}
