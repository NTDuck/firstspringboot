package com.viettelsoftware.firstspringboot.service;

import lombok.NonNull;

public interface TaskExportGenerator {
    byte @NonNull [] generate();
}
