package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.service.model.AuthenticatedUser;
import lombok.NonNull;

import java.util.Optional;

public interface AuthenticationService {
    Optional<@NonNull AuthenticatedUser> getCurrentAuthenticatedUser();
}
