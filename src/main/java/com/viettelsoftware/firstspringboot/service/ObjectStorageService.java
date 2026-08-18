package com.viettelsoftware.firstspringboot.service;

import lombok.NonNull;

import java.io.InputStream;
import java.time.Duration;

public interface ObjectStorageService {

    void put(@NonNull String objectKey, @NonNull InputStream file, long size, @NonNull String contentType);

    void put(@NonNull String objectKey, byte @NonNull [] file, @NonNull String contentType);

    void delete(@NonNull String objectKey);

    @NonNull String createPresignedDownloadUrl(@NonNull String objectKey, @NonNull Duration expiration);

    @NonNull String createPresignedUploadUrl(@NonNull String objectKey, @NonNull Duration expiration);

    @NonNull InputStream get(@NonNull String objectKey);

    boolean exists(@NonNull String objectKey);
}
