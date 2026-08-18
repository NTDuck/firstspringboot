package com.viettelsoftware.firstspringboot.service;

import lombok.NonNull;

import java.time.Duration;

public interface ObjectStorageService {

    void put(@NonNull String objectKey, byte @NonNull [] file, @NonNull String contentType);

    void delete(@NonNull String objectKey);

    @NonNull String createPresignedDownloadUrl(@NonNull String objectKey, @NonNull Duration expiration);
}
