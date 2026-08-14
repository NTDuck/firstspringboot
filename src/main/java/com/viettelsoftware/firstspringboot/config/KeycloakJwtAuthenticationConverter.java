package com.viettelsoftware.firstspringboot.config;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// https://www.baeldung.com/spring-boot-keycloak
@Component
public class KeycloakJwtAuthenticationConverter extends JwtAuthenticationConverter {
    public KeycloakJwtAuthenticationConverter() {
        setPrincipalClaimName("preferred_username");
        setJwtGrantedAuthoritiesConverter(
                jwt -> keycloakRealmRolesAuthoritiesConverter.convert(jwt.getClaims())
        );
    }

    @Autowired
    private KeycloakRealmRolesAuthoritiesConverter keycloakRealmRolesAuthoritiesConverter;

    @Component
    private static class KeycloakRealmRolesAuthoritiesConverter
            implements Converter<Map<@NonNull String, @NonNull Object>, Collection<@NonNull GrantedAuthority>> {
        @Override
        @SuppressWarnings("unchecked")
        public Collection<@NonNull GrantedAuthority> convert(Map<@NonNull String, @NonNull Object> claims) {
            var realmAccess = Optional.ofNullable((Map<@NonNull String, @NonNull Object>) claims.get("realm_access"));
            var roles = realmAccess.flatMap(map -> Optional.ofNullable((List<@NonNull String>) map.get("roles")));

            return roles.stream()
                    .flatMap(Collection::stream)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
    }
}