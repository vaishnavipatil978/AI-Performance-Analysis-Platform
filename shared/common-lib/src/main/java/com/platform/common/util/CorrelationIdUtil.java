package com.platform.common.util;

import java.util.UUID;

public final class CorrelationIdUtil {

    public static final String HEADER_NAME = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private CorrelationIdUtil() {
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static void set(String id) {
        HOLDER.set(id);
    }

    public static String get() {
        return HOLDER.get();
    }

    public static String getOrGenerate() {
        String id = HOLDER.get();
        if (id == null || id.isBlank()) {
            id = generate();
            HOLDER.set(id);
        }
        return id;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
