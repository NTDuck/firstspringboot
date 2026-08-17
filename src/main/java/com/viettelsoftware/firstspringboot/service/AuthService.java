package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import lombok.NonNull;

public interface AuthService {
    @NonNull CurrentUser getCurrentUser();
}
