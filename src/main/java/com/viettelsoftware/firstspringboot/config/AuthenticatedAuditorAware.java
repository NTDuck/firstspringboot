package com.viettelsoftware.firstspringboot.config;

import com.viettelsoftware.firstspringboot.service.model.AuthenticatedUser;
import com.viettelsoftware.firstspringboot.service.AuthenticationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticatedAuditorAware implements AuditorAware<@NonNull Long> {

    private final AuthenticationService authenticationService;

    @NotNull
    @Override
    public Optional<@NonNull Long> getCurrentAuditor() {
        return authenticationService
                .getCurrentAuthenticatedUser()
                .map(AuthenticatedUser::getId);
    }
}
