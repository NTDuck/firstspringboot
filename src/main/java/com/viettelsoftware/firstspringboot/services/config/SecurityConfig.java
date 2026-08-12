package com.viettelsoftware.firstspringboot.services.config;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        return http
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(csrf -> csrf.disable())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(accessDeniedHandler_()))
                .authorizeRequests(auth -> auth
                        .antMatchers("/actuator/health").permitAll()
                        .antMatchers("/profile").authenticated()
                        .antMatchers(HttpMethod.GET, "/api/v1/**").hasAuthority("GET")
                        .antMatchers(HttpMethod.POST, "/api/v1/**").hasAuthority("POST")
                        .antMatchers(HttpMethod.PUT, "/api/v1/**").hasAuthority("PUT")
                        .antMatchers(HttpMethod.DELETE, "/api/v1/**").hasAuthority("DELETE")
                        .anyRequest().denyAll()
                )
                .build();
    }



    @Bean
    public Converter<Map<String, Object>, Collection<GrantedAuthority>> realmRolesAuthoritiesConverter() {
        return RealmRolesAuthoritiesConverter.of();
    }

    @NoArgsConstructor(staticName = "of")
    public static class RealmRolesAuthoritiesConverter
            implements Converter<Map<String, Object>, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Map<String, Object> claims) {
            var realmAccess = Optional.ofNullable((Map<String, Object>) claims.get("realm_access"));
            var roles = realmAccess.flatMap(map -> Optional.ofNullable((List<String>) map.get("roles")));

            return roles
                    .map(List::stream)
                    .orElseGet(java.util.stream.Stream::empty)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            Converter<Map<String, Object>, Collection<GrantedAuthority>> realmRolesAuthoritiesConverter) {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> realmRolesAuthoritiesConverter.convert(jwt.getClaims()));

        return converter;
    }

//    @Bean
//    public AccessDeniedHandler accessDeniedHandler_() {
//        return (request, response, exception) -> {
//            Authentication authentication =
//                    SecurityContextHolder.getContext().getAuthentication();
//
//            String username = Optional.ofNullable(authentication)
//                    .map(Authentication::getName)
//                    .orElse("N/A");
//
//            String role = Optional.ofNullable(authentication)
//                    .map(Authentication::getAuthorities)
//                    .flatMap(authorities -> authorities.stream().findFirst())
//                    .map(GrantedAuthority::getAuthority)
//                    .orElse("N/A");
//
//            val error = AccessDeniedException_.of(username, role);
//
//            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//            response.getWriter().write(
//                    String.format("{\"message\":\"%s\"}", error.getMessage())
//            );
//        };
//    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler_() {
        return (request, response, exception) -> {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String username = "N/A";
            String role = "N/A";

            if (authentication instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtAuthentication =
                        (JwtAuthenticationToken) authentication;

                Jwt jwt = jwtAuthentication.getToken();

                username = Optional.ofNullable(
                        jwt.getClaimAsString("preferred_username")
                ).orElse("N/A");

                Map<String, Object> realmAccess =
                        jwt.getClaim("realm_access");

                if (realmAccess != null) {
                    Object rolesObject = realmAccess.get("roles");

                    if (rolesObject instanceof List) {
                        List<?> roles = (List<?>) rolesObject;

                        if (!roles.isEmpty()) {
                            role = String.valueOf(roles.get(0));
                        }
                    }
                }
            }

            AccessDeniedException_ error =
                    AccessDeniedException_.of(username, role);

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                    String.format(
                            "{\"message\":\"%s\"}",
                            error.getMessage()
                    )
            );
        };
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class AccessDeniedException_ extends Exception {
        private static final long serialVersionUID = 1L;

        public static @NonNull SecurityConfig.AccessDeniedException_ of(@NonNull String username, @NonNull String role) {
            return new AccessDeniedException_(String.format("User %s denied access (must have role `%s`)", username, role));
        }

        private AccessDeniedException_(@NonNull String message) {
            super(message);
        }
    }
}