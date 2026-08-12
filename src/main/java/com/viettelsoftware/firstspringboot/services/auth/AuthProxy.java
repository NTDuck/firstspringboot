package com.viettelsoftware.firstspringboot.services.auth;

import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthProxy {
    public interface AuthoritiesConverter extends Converter<Map<String, Object>, Collection<GrantedAuthority>> {}

    @Bean
    AuthoritiesConverter realmRolesAuthoritiesConverter() {
        return claims -> {
            var realmAccess = Optional.ofNullable((Map<String, Object>) claims.get("realm_access"));
            var roles = realmAccess.flatMap(map -> Optional.ofNullable((List<String>) map.get("roles")));
            return roles.map(List::stream)
                    .orElse(Stream.empty())
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .collect(Collectors.toList());
        };
    }

    @Bean
    GrantedAuthoritiesMapper authenticationConverter(AuthoritiesConverter authoritiesConverter) {
        return (authorities) -> authorities.stream()
                .filter(authority -> authority instanceof OidcUserAuthority)
                .map(OidcUserAuthority.class::cast)
                .map(OidcUserAuthority::getIdToken)
                .map(OidcIdToken::getClaims)
                .map(authoritiesConverter::convert)
                .flatMap(roles -> roles.stream())
                .collect(Collectors.toSet());
    }

    @Bean
    SecurityFilterChain clientSecurityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http.oauth2Login(Customizer.withDefaults());
        http.logout((logout) -> {
            var logoutSuccessHandler =
                    new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
            logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/");
            logout.logoutSuccessHandler(logoutSuccessHandler);
        });

        http.authorizeRequests()
            .antMatchers(HttpMethod.GET, "/**").hasAuthority("GET")
            .antMatchers(HttpMethod.POST, "/**").hasAuthority("POST")
            .antMatchers(HttpMethod.PUT, "/**").hasAuthority("PUT")
            .antMatchers(HttpMethod.DELETE, "/**").hasAuthority("DELETE")

            .antMatchers("/login").permitAll()
            .anyRequest().authenticated();

        return http.build();
    }

//        @Bean
//    public @NonNull SecurityFilterChain filterChain(@NonNull HttpSecurity http) throws Exception {
//        http
//            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            .and()
//            .csrf().disable()
//            .authorizeRequests()
//                .antMatchers(HttpMethod.GET, "/**").hasAuthority("GET")
//                .antMatchers(HttpMethod.POST, "/**").hasAuthority("POST")
//                .antMatchers(HttpMethod.PUT, "/**").hasAuthority("PUT")
//                .antMatchers(HttpMethod.DELETE, "/**").hasAuthority("DELETE")
//
//                .antMatchers("/login").permitAll()
//                .anyRequest().authenticated()
//            .and()
//            .oauth2ResourceServer().jwt();
//
//        return http.build();
//    }

    @Bean
    JwtAuthenticationConverter authenticationConverter_(AuthoritiesConverter authoritiesConverter) {
        var authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            return authoritiesConverter.convert(jwt.getClaims());
        });
        return authenticationConverter;
    }

    @Bean
    SecurityFilterChain resourceServerSecurityFilterChain(
            HttpSecurity http,
            Converter<Jwt, AbstractAuthenticationToken> authenticationConverter) throws Exception {
        http.oauth2ResourceServer(resourceServer -> {
            resourceServer.jwt(jwtDecoder -> {
                jwtDecoder.jwtAuthenticationConverter(authenticationConverter);
            });
        });

        http.sessionManagement(sessions -> {
            sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }).csrf(csrf -> {
            csrf.disable();
        });

        http.authorizeRequests()
                .antMatchers("/me").authenticated()
                .anyRequest().denyAll();

        return http.build();
    }
}
