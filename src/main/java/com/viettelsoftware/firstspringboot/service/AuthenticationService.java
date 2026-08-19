package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.service.dto.AuthenticatedUserDto;
import lombok.NonNull;

import java.util.Optional;

public interface AuthenticationService {
    Optional<@NonNull AuthenticatedUserDto> getCurrentAuthenticatedUser();
}
