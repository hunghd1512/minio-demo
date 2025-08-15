package com.example.miniodemo.service;

import io.minio.MinioClient;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresignedUrlService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    /**
     * Generates a presigned URL for uploading an object.
     * Using when is bucket private
     *
     * @param objectName The name of the object to upload
     * @param expiryTime The time in minutes after which the URL will expire
     * @return The presigned URL for uploading
     */
    public String getPresignedUploadUrl(String objectName, int expiryTime) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiryTime, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            log.error("Error generating presigned upload URL for object '{}': {}", objectName, e.getMessage());
            return null;
        }
    }

    /**
     * Generates a presigned URL for downloading an object.
     *
     * @param objectName The name of the object to download
     * @param expiryTime The time in minutes after which the URL will expire
     * @return The presigned URL for downloading
     */
    public String getPresignedDownloadUrl(String objectName, int expiryTime) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiryTime, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            log.error("Error generating presigned download URL for object '{}': {}", objectName, e.getMessage());
            return null;
        }
    }
}