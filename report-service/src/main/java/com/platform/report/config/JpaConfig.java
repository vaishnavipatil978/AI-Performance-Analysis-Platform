package com.platform.report.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            try {
                var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                String userId = attrs.getRequest().getHeader("X-User-ID");
                return Optional.ofNullable(userId).filter(s -> !s.isBlank());
            } catch (Exception e) {
                return Optional.of("system");
            }
        };
    }
}
