package com.viettelsoftware.firstspringboot.config;

import com.viettelsoftware.firstspringboot.controller.exception.abc.BaseGloballyHandledException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// https://jenkov.com/tutorials/java-servlets/servlet-filters.html
// https://stackoverflow.com/questions/34595605/how-to-manage-exceptions-thrown-in-filters-in-spring
@Component
public class SecurityFilterChainExceptionHandler extends OncePerRequestFilter {
    private final HandlerExceptionResolver resolver;

    public SecurityFilterChainExceptionHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BaseGloballyHandledException exception) {
            // Routes to `GlobalExceptionHandler`
            resolver.resolveException(request, response, null, exception);
        }
    }

}
