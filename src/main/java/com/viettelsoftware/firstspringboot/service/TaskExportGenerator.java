package com.viettelsoftware.firstspringboot.service;

import lombok.NonNull;

import java.io.File;

public interface TaskExportGenerator {
    @NonNull File generate();
}
