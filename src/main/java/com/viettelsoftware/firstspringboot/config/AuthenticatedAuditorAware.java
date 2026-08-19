package com.viettelsoftware.firstspringboot.config;

import com.viettelsoftware.firstspringboot.service.dto.AuthenticatedUserDto;
import com.viettelsoftware.firstspringboot.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticatedAuditorAware implements AuditorAware<Long> {

    private final AuthenticationService authenticationService;

    @NotNull
    @Override
    public Optional<Long> getCurrentAuditor() {
        return authenticationService
                .getCurrentAuthenticatedUser()
                .map(AuthenticatedUserDto::getId);
    }
}
