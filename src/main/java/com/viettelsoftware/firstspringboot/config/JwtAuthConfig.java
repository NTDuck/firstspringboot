package com.viettelsoftware.firstspringboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class JwtAuthConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            RealmRolesAuthoritiesConverter realmRolesAuthoritiesConverter) {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> realmRolesAuthoritiesConverter.convert(jwt.getClaims()));
        return converter;
    }
}
