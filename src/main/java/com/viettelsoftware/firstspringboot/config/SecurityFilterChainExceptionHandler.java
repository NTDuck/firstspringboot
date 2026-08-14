package com.viettelsoftware.firstspringboot.config;

import com.viettelsoftware.firstspringboot.exception.abc.FirstspringbootApplicationException;
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
// Routes to `GlobalExceptionHandler`
@Component
public class SecurityFilterChainExceptionHandler extends OncePerRequestFilter {
    public SecurityFilterChainExceptionHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (FirstspringbootApplicationException exception) {
            resolver.resolveException(request, response, null, exception);
        }
    }

    private final HandlerExceptionResolver resolver;
}
