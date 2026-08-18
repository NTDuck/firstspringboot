package com.viettelsoftware.firstspringboot.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.NonNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.Duration;

@Service
public class MinIOObjectStorageService implements ObjectStorageService, InitializingBean {

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
    public void put(@NonNull String objectKey, byte @NonNull [] file, @NonNull String contentType) {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(new ByteArrayInputStream(file), file.length, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception exception) {
            throw new RuntimeException("Failed to upload file to MinIO", exception);
        }
    }

    @Override
    public void delete(@NonNull String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception exception) {
            throw new RuntimeException("Failed to delete file from MinIO", exception);
        }
    }

    @Override
    public @NonNull String createPresignedDownloadUrl(@NonNull String objectKey, @NonNull Duration expiration) {
        try {
            int expirySeconds = (int) Math.max(1, Math.min(expiration.getSeconds(), 7 * 24 * 3600));
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(expirySeconds)
                            .build()
            );
        } catch (Exception exception) {
            throw new RuntimeException("Failed to generate presigned download URL from MinIO", exception);
        }
    }
}
