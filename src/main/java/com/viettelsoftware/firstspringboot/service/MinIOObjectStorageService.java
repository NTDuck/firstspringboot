package com.viettelsoftware.firstspringboot.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;

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
            ensureBucketExists();

            val args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(file, size, -1)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(args);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to upload file to MinIO", exception);
        }
    }

    @Override
    public void put(@NonNull String objectKey, byte @NonNull [] file, @NonNull String contentType) {
        put(objectKey, new ByteArrayInputStream(file), file.length, contentType);
    }

    @Override
    public void delete(@NonNull String objectKey) {
        try {
            val args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build();

            minioClient.removeObject(args);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to delete file from MinIO", exception);
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
            throw new RuntimeException("Failed to generate presigned download URL from MinIO", exception);
        }
    }

    @Override
    public @NonNull String createPresignedUploadUrl(@NonNull String objectKey, @NonNull Duration expiration) {
        try {
            ensureBucketExists();

            val expirySeconds = clampExpiry(expiration);
            val args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry(expirySeconds)
                    .build();

            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to generate presigned upload URL from MinIO", exception);
        }
    }

    @Override
    public @NonNull InputStream get(@NonNull String objectKey) {
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
