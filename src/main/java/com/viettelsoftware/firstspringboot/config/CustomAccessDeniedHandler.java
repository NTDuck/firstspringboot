package com.viettelsoftware.firstspringboot.config;

import com.viettelsoftware.firstspringboot.auth.exception.InsufficientRoleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    @Lazy
    private RequestMappingHandlerMapping handlerMapping;

    private static final Pattern ROLE_PATTERN = Pattern.compile("(?:hasAuthority|hasRole|hasAnyAuthority|hasAnyRole)\\(['\"]?([^'\"]+)['\"]?\\)");

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = "N/A";
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            String preferredUsername = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (preferredUsername != null) {
                username = preferredUsername;
            } else {
                username = jwtAuth.getName();
            }
        } else if (authentication != null) {
            username = authentication.getName();
        }

        throw InsufficientRoleException.builder()
                .username(username)
                .role(resolveRequiredRole(request))
                .build();
    }

    private String resolveRequiredRole(HttpServletRequest request) {
        try {
            Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
            if (!(handler instanceof HandlerMethod) && handlerMapping != null) {
                HandlerExecutionChain chain = handlerMapping.getHandler(request);
                if (chain != null) {
                    handler = chain.getHandler();
                }
            }
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                PreAuthorize preAuthorize = handlerMethod.getMethodAnnotation(PreAuthorize.class);
                if (preAuthorize == null) {
                    preAuthorize = handlerMethod.getBeanType().getAnnotation(PreAuthorize.class);
                }
                if (preAuthorize != null) {
                    String value = preAuthorize.value();
                    Matcher matcher = ROLE_PATTERN.matcher(value);
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                    return value;
                }
            }
        } catch (Exception ignored) {
        }
        return "N/A";
    }
}
