package com.platform.gateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MutableHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> customHeaders = new LinkedHashMap<>();

    public MutableHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    public void putHeader(String name, String value) {
        customHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        String override = customHeaders.get(name);
        return override != null ? override : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String override = customHeaders.get(name);
        if (override != null) {
            return Collections.enumeration(List.of(override));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new LinkedHashSet<>(customHeaders.keySet());
        Enumeration<String> originalNames = super.getHeaderNames();
        if (originalNames != null) {
            while (originalNames.hasMoreElements()) {
                names.add(originalNames.nextElement());
            }
        }
        return Collections.enumeration(names);
    }
}
