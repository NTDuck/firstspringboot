package com.viettelsoftware.firstspringboot.config;

import com.viettelsoftware.firstspringboot.exception.InsufficientAuthorizationException;
import lombok.val;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

@Component
public class AccessDeniedExceptionTranslator implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        val username = authentication.getName();
        val operation = resolveOperation(request);

        throw InsufficientAuthorizationException.builder()
                .username(username)
                .operation(operation)
                .build();
    }

    private String resolveOperation(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE))
                .filter(HandlerMethod.class::isInstance)
                .map(HandlerMethod.class::cast)
                .map(HandlerMethod::getMethod)
                .map(Method::getName)
                .orElse("N/A");
    }
}
