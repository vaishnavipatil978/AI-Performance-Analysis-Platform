package com.platform.common.web;

import com.platform.common.util.CorrelationIdUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String correlationId = httpRequest.getHeader(CorrelationIdUtil.HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = CorrelationIdUtil.generate();
        }

        CorrelationIdUtil.set(correlationId);
        MDC.put(CorrelationIdUtil.MDC_KEY, correlationId);
        httpResponse.setHeader(CorrelationIdUtil.HEADER_NAME, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            CorrelationIdUtil.clear();
            MDC.remove(CorrelationIdUtil.MDC_KEY);
        }
    }
}
