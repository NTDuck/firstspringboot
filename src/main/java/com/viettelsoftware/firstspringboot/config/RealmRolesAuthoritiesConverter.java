package com.viettelsoftware.firstspringboot.config;

import lombok.NoArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor(staticName = "of")
public class RealmRolesAuthoritiesConverter
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
