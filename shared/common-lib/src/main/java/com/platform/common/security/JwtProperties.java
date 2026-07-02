package com.platform.common.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpiryMs = 900000L;
    private long refreshTokenExpiryMs = 604800000L;
}
