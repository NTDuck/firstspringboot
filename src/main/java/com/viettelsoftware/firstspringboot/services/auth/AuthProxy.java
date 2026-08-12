package com.viettelsoftware.firstspringboot.services.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuthProxy {

    @Bean
    public Converter<Map<String, Object>, Collection<GrantedAuthority>>
    realmRolesAuthoritiesConverter() {
        return claims -> {
            var realmAccess = Optional.ofNullable((Map<String, Object>) claims.get("realm_access"));
            var roles = realmAccess.flatMap(map -> Optional.ofNullable((List<String>) map.get("roles")));

            return roles
                    .map(List::stream)
                    .orElseGet(java.util.stream.Stream::empty)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            Converter<Map<String, Object>, Collection<GrantedAuthority>>
                    realmRolesAuthoritiesConverter) {

        var converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                jwt -> realmRolesAuthoritiesConverter.convert(jwt.getClaims())
        );

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {

        http
                .sessionManagement(sessions ->
                        sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(csrf -> csrf.disable())
                .oauth2ResourceServer(resourceServer ->
                        resourceServer.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                )
                .authorizeRequests(auth -> auth
                        .antMatchers("/actuator/health").permitAll()
                        .antMatchers("/authenticate").authenticated()
                        .antMatchers(HttpMethod.GET, "/api/v1/**").hasAuthority("GET")
                        .antMatchers(HttpMethod.POST, "/api/v1/**").hasAuthority("POST")
                        .antMatchers(HttpMethod.PUT, "/api/v1/**").hasAuthority("PUT")
                        .antMatchers(HttpMethod.DELETE, "/api/v1/**").hasAuthority("DELETE")
                        .anyRequest().denyAll()
                );

        return http.build();
    }
}