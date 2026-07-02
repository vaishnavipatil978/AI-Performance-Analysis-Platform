package com.platform.gateway.filter;

import com.platform.common.util.CorrelationIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdGatewayFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String correlationId = request.getHeader(CorrelationIdUtil.HEADER_NAME);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = CorrelationIdUtil.generate();
        }

        // Propagate to downstream services via the request wrapper
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
        mutableRequest.putHeader(CorrelationIdUtil.HEADER_NAME, correlationId);

        // Expose correlation ID on the response so callers can trace requests
        response.setHeader(CorrelationIdUtil.HEADER_NAME, correlationId);

        CorrelationIdUtil.set(correlationId);
        try {
            chain.doFilter(mutableRequest, response);
        } finally {
            CorrelationIdUtil.clear();
        }
    }
}
