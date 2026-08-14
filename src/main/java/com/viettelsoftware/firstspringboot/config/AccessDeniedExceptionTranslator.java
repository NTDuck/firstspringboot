package com.viettelsoftware.firstspringboot.config;

import com.viettelsoftware.firstspringboot.config.exception.InsufficientAuthorizationException;
import lombok.NonNull;
import lombok.val;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class AccessDeniedExceptionTranslator implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        val username = authentication.getName();
        val authorizationExpression = resolveAuthorizationExpression(request);

        throw InsufficientAuthorizationException.builder()
                .username(username)
                .authorizationExpression(authorizationExpression)
                .build();
    }

    private String resolveAuthorizationExpression(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE))
                .filter(HandlerMethod.class::isInstance)
                .map(HandlerMethod.class::cast)
                .map(this::getPreAuthorize)
                .map(PreAuthorize::value)
                .orElse("N/A");
    }

    private @NonNull PreAuthorize getPreAuthorize(HandlerMethod handlerMethod) {
        return Optional.ofNullable(handlerMethod.getMethodAnnotation(PreAuthorize.class))
                .orElseGet(() -> handlerMethod.getBeanType().getAnnotation(PreAuthorize.class));
    }
}
