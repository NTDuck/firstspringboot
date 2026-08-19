package com.viettelsoftware.firstspringboot.config;

import com.viettelsoftware.firstspringboot.config.properties.DisplayProperties;
import com.viettelsoftware.firstspringboot.controller.exception.InsufficientAuthorizationException;
import com.viettelsoftware.firstspringboot.service.AuthenticationService;
import com.viettelsoftware.firstspringboot.service.TraceService;
import com.viettelsoftware.firstspringboot.service.model.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.AccessDeniedException;
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

// This Component lies in the Security Filter Chain
// therefore uses `app.display.null-value`
// instead of throwing like other Services
@RequiredArgsConstructor
@Component
public class AccessDeniedExceptionTranslator implements AccessDeniedHandler {

    private final AuthenticationService authenticationService;
    private final DisplayProperties displayProperties;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
            throws IOException, ServletException {

        val username = getUsername();
        val operation = getOperationFromHttpServletRequest(request);

        throw InsufficientAuthorizationException.builder()
                .username(username)
                .operation(operation)
                .build();
    }

    private String getUsername() {
        return authenticationService
                .getCurrentAuthenticatedUser()
                .map(AuthenticatedUser::getName)
                .orElse(displayProperties.getNullValue());
    }

    private String getOperationFromHttpServletRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE))
                .filter(HandlerMethod.class::isInstance)
                .map(HandlerMethod.class::cast)
                .map(HandlerMethod::getMethod)
                .map(Method::getName)
                .orElse(displayProperties.getNullValue());
    }
}
