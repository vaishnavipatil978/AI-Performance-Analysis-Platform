package com.platform.gateway.config;

import com.platform.common.security.JwtProperties;
import com.platform.common.security.JwtUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class GatewayConfig {

    @Bean
    public JwtUtil jwtUtil(JwtProperties jwtProperties) {
        return new JwtUtil(
                jwtProperties.getSecret(),
                jwtProperties.getAccessTokenExpiryMs(),
                jwtProperties.getRefreshTokenExpiryMs()
        );
    }
}
