package com.viettelsoftware.firstspringboot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .addFilterBefore(securityFilterChainExceptionHandler, LogoutFilter.class)
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeRequests(auth -> auth
                        .antMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .build();
    }

    @Autowired
    private SecurityFilterChainExceptionHandler securityFilterChainExceptionHandler;

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;
}
