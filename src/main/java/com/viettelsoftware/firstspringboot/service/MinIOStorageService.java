package com.viettelsoftware.firstspringboot.service;

import lombok.NonNull;

public interface MinIOStorageService {

    @NonNull String uploadFileAndGetPresignedUrl(@NonNull String objectName, byte @NonNull [] content, @NonNull String contentType);
}
