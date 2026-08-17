package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import org.springframework.lang.Nullable;

public interface AuthService {
    @Nullable CurrentUser getCurrentUser();
}
