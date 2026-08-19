package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.repository.UserRepository;
import com.viettelsoftware.firstspringboot.service.dto.AuthenticatedUserDto;
import com.viettelsoftware.firstspringboot.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    @Override
    public Optional<AuthenticatedUserDto> getCurrentAuthenticatedUser() {
        val authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken)) {
            return Optional.empty();
        }

        val jwt = ((JwtAuthenticationToken) authentication).getToken();

        val subject = getSubjectFromJwt(jwt);
        if (subject.isEmpty()) return Optional.empty();

        val userId = getUserIdFromSubject(subject.get());
        if (userId.isEmpty()) return Optional.empty();

        val username = getUsernameFromJwt(jwt);
        if (username.isEmpty()) return Optional.empty();

        val roles = getUserRolesFromAuthentication(authentication);

        return Optional.of(
                AuthenticatedUserDto.builder()
                        .id(userId.get())
                        .name(username.get())
                        .roles(roles)
                        .build());
    }

    private Optional<String> getSubjectFromJwt(Jwt jwt) {
        val subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) return Optional.empty();
        return Optional.of(subject);
    }

    private Optional<Long> getUserIdFromSubject(String subject) {
        try {
            // Subject is UID
            return Optional.of(Long.parseLong(subject));
        } catch (NumberFormatException exception) {
            // Subject is Keycloak UID
            return userRepository.findByKeycloakId(subject)
                    .map(User::getId);
        }
    }

    private Optional<String> getUsernameFromJwt(Jwt jwt) {
        val username = jwt.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME);

        if (username == null || username.isBlank()) return Optional.empty();
        return Optional.of(username);
    }

    private List<String> getUserRolesFromAuthentication(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}
