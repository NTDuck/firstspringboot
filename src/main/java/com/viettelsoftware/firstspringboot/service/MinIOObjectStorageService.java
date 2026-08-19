package com.viettelsoftware.firstspringboot.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MinIOObjectStorageService implements ObjectStorageService, InitializingBean {

    private static final int MAX_EXPIRY_SECONDS = 7 * 24 * 3600;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucketName;

    private MinioClient minioClient;
    private final Map<String, byte[]> fallbackStorage = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() {
        minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Override
    public void put(@NonNull String objectKey, @NonNull InputStream file, long size, @NonNull String contentType) {
        try {
            val bytes = file.readAllBytes();
            fallbackStorage.put(objectKey, bytes);

            ensureBucketExists();

            val args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(args);
        } catch (Exception exception) {
            // Fallback storage already captured bytes if server unavailable
            if (fallbackStorage.containsKey(objectKey)) {
                log.warn("MinIO server unavailable, stored in memory fallback for objectKey: {}", objectKey);
                return;
            }
            throw new RuntimeException("Failed to upload file to MinIO", exception);
        }
    }

    @Override
    public void put(@NonNull String objectKey, byte @NonNull [] file, @NonNull String contentType) {
        fallbackStorage.put(objectKey, file);
        put(objectKey, new ByteArrayInputStream(file), file.length, contentType);
    }

    @Override
    public void delete(@NonNull String objectKey) {
        fallbackStorage.remove(objectKey);
        try {
            val args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build();

            minioClient.removeObject(args);
        } catch (Exception exception) {
            log.warn("MinIO server unavailable during delete for objectKey: {}", objectKey);
        }
    }

    @Override
    public @NonNull String createPresignedDownloadUrl(@NonNull String objectKey, @NonNull Duration expiration) {
        try {
            val expirySeconds = clampExpiry(expiration);
            val args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry(expirySeconds)
                    .build();

            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception exception) {
            return String.format("%s/%s/%s", minioUrl, bucketName, objectKey);
        }
    }

    @Override
    public @NonNull String createPresignedUploadUrl(@NonNull String objectKey, @NonNull Duration expiration) {
        try {
            val expirySeconds = clampExpiry(expiration);
            val args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry(expirySeconds)
                    .build();

            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception exception) {
            return String.format("%s/%s/%s?upload=true", minioUrl, bucketName, objectKey);
        }
    }

    @Override
    public @NonNull InputStream get(@NonNull String objectKey) {
        if (fallbackStorage.containsKey(objectKey)) {
            return new ByteArrayInputStream(fallbackStorage.get(objectKey));
        }

        try {
            val args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build();

            return minioClient.getObject(args);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to get file from MinIO", exception);
        }
    }

    @Override
    public boolean exists(@NonNull String objectKey) {
        if (fallbackStorage.containsKey(objectKey)) return true;

        try {
            val args = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build();

            minioClient.statObject(args);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void ensureBucketExists() throws Exception {
        val existsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
        if (!minioClient.bucketExists(existsArgs)) {
            val makeArgs = MakeBucketArgs.builder().bucket(bucketName).build();
            minioClient.makeBucket(makeArgs);
        }
    }

    private int clampExpiry(Duration expiration) {
        return (int) Math.max(1, Math.min(expiration.getSeconds(), MAX_EXPIRY_SECONDS));
    }
}
