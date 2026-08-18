package com.viettelsoftware.firstspringboot.service;

import lombok.NonNull;

public interface UserExportGenerator {
    byte @NonNull [] generate();
}
