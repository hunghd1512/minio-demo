package com.example.miniodemo.service;

import com.example.miniodemo.dto.BucketMetrics;
import com.example.miniodemo.dto.FileInfo;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.*;
import io.minio.messages.Retention;
import io.minio.messages.ObjectLockConfiguration;
import io.minio.messages.RetentionMode;
import io.minio.messages.RetentionDuration;
import io.minio.messages.RetentionDurationDays;
import io.minio.messages.Expiration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    private final PresignedUrlService presignedUrlService;

    @Value("${minio.baseUrl}")
    private String baseUrl;

    @Value("${minio.bucket}")
    private String bucketName;

    public boolean doesBucketExist() {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("Error checking bucket existence: {}", e.getMessage());
            return false;
        }
    }

    public void createBucket() {
        try {
            if (!doesBucketExist()) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' created successfully", bucketName);
            } else {
                log.info("Bucket '{}' already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("Error creating bucket: {}", e.getMessage());
        }
    }

    public FileInfo uploadFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            
            log.info("File '{}' uploaded successfully", fileName);
            
            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(fileName);
            String url = presignedUrlService.getPresignedDownloadUrl(fileName,10); // private bucket
//            String url2 = baseUrl + bucketName + "/" + fileName;   // public bucket
            fileInfo.setUrl(url);
            fileInfo.setSize(file.getSize());
            fileInfo.setContentType(file.getContentType());
            
            return fileInfo;
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            return null;
        }
    }

    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage());
            return null;
        }
    }

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("File '{}' deleted successfully", fileName);
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
        }
    }

    public List<FileInfo> listFiles() {
        List<FileInfo> files = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(item.objectName());
                fileInfo.setSize(item.size());
                fileInfo.setUrl(baseUrl + "/" + bucketName + "/" + item.objectName());
                files.add(fileInfo);
            }
        } catch (Exception e) {
            log.error("Error listing files: {}", e.getMessage());
        }
        return files;
    }

    /**
     * 1. Object Lifecycle Management
     * Implement automatic object expiration and transition rules
     */
//    public void setObjectLifecycleRules() {
//        try {
//            // Create lifecycle configuration for expiring objects after 30 days
//            // Using the correct classes for lifecycle rules
//            LifecycleRule rule = new LifecycleRule(
//                io.minio.messages.LifecycleRule.Status.ENABLED,
//                null,
//                new Expiration(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)),  // Expire after 30 days
//                null,
//                null,
//                null,
//                null,
//                null
//            );
//
//            List<LifecycleRule> rules = new ArrayList<>();
//            rules.add(rule);
//
//            LifecycleConfiguration config = new LifecycleConfiguration(rules);
//
//            minioClient.setBucketLifecycle(
//                SetBucketLifecycleArgs.builder()
//                    .bucket(bucketName)
//                    .config(config)
//                    .build()
//            );
//
//            log.info("Lifecycle rules set successfully for bucket '{}'", bucketName);
//        } catch (Exception e) {
//            log.error("Error setting lifecycle rules: {}", e.getMessage());
//        }
//    }

    /**
     * 2. Object Versioning
     * Enable and manage object versioning for better data protection
     */
//    public void enableBucketVersioning() {
//        try {
//            minioClient.setBucketVersioning(
//                SetBucketVersioningArgs.builder()
//                    .bucket(bucketName)
//                    .config(new VersioningConfiguration(VersioningConfiguration.STATE_ENABLED, null))
//                    .build()
//            );
//
//            log.info("Versioning enabled successfully for bucket '{}'", bucketName);
//        } catch (Exception e) {
//            log.error("Error enabling versioning: {}", e.getMessage());
//        }
//    }

    public List<String> listObjectVersions(String objectName) {
        List<String> versions = new ArrayList<>();
        try {
            // Note: Minio Java SDK does not have a direct method to list object versions
            // This is a placeholder implementation. In practice, you would need to use
            // a different approach or check if the SDK has been updated with this feature.
            log.warn("Listing object versions is not directly supported by the Minio Java SDK. This is a placeholder implementation.");
            versions.add("Version listing not implemented");
        } catch (Exception e) {
            log.error("Error listing object versions: {}", e.getMessage());
        }
        return versions;
    }

    /**
     * 3. Server-Side Encryption
     * Implement server-side encryption for sensitive data
     */
    public FileInfo uploadEncryptedFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            
            // Generate a 256-bit AES key for SSE-C
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();
            
            // Create SSE-C with customer key
            // Using a more standard approach for server-side encryption
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Amz-Server-Side-Encryption-Customer-Algorithm", "AES256");
            headers.put("X-Amz-Server-Side-Encryption-Customer-Key", Base64.getEncoder().encodeToString(secretKey.getEncoded()));
            headers.put("X-Amz-Server-Side-Encryption-Customer-Key-MD5", 
                java.util.Base64.getEncoder().encodeToString(
                    java.security.MessageDigest.getInstance("MD5").digest(secretKey.getEncoded())
                )
            );
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .headers(headers)  // Add server-side encryption headers
                    .build()
            );
            
            log.info("Encrypted file '{}' uploaded successfully", fileName);
            
            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(fileName);
            fileInfo.setUrl(baseUrl + "/" + bucketName + "/" + fileName);
            fileInfo.setSize(file.getSize());
            fileInfo.setContentType(file.getContentType());
            
            return fileInfo;
        } catch (Exception e) {
            log.error("Error uploading encrypted file: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 4. Object Tagging and Metadata
     * Enhance file management with tags and custom metadata
     */
//    public FileInfo uploadFileWithTagging(MultipartFile file, Map<String, String> tags) {
//        try {
//            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//
//            // Add custom metadata
//            Map<String, String> headers = new HashMap<>();
//            headers.put("X-Amz-Meta-Custom-Id", "custom-value");
//            headers.put("X-Amz-Meta-Upload-User", "user-id");
//
//            minioClient.putObject(
//                PutObjectArgs.builder()
//                    .bucket(bucketName)
//                    .object(fileName)
//                    .stream(file.getInputStream(), file.getSize(), -1)
//                    .contentType(file.getContentType())
//                    .headers(headers)  // Add custom headers
//                    .tagging(new Tags(tags))  // Add tags
//                    .build()
//            );
//
//            log.info("File '{}' with tags uploaded successfully", fileName);
//
//            FileInfo fileInfo = new FileInfo();
//            fileInfo.setName(fileName);
//            fileInfo.setUrl(baseUrl + "/" + bucketName + "/" + fileName);
//            fileInfo.setSize(file.getSize());
//            fileInfo.setContentType(file.getContentType());
//
//            return fileInfo;
//        } catch (Exception e) {
//            log.error("Error uploading file with tags: {}", e.getMessage());
//            return null;
//        }
//    }

    public Map<String, String> getObjectTags(String objectName) {
        try {
            Tags tags = minioClient.getObjectTags(
                GetObjectTagsArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            return tags.get();
        } catch (Exception e) {
            log.error("Error getting object tags: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 7. Object Locking (WORM - Write Once Read Many)
     * Implement compliance features with object locking
     */
    public void enableObjectLocking() {
        try {
            // Note: Object locking must be enabled when creating the bucket
            // This will only work if the bucket was created with object locking enabled
            minioClient.setObjectLockConfiguration(
                SetObjectLockConfigurationArgs.builder()
                    .bucket(bucketName)
                    .config(new ObjectLockConfiguration(RetentionMode.COMPLIANCE, new RetentionDurationDays(365)))
                    .build()
            );
            
            log.info("Object locking enabled successfully for bucket '{}'", bucketName);
        } catch (Exception e) {
            log.error("Error enabling object locking: {}", e.getMessage());
        }
    }

    public FileInfo uploadWithRetention(MultipartFile file, RetentionMode mode, ZonedDateTime duration) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .retention(new Retention(mode, duration))  // Add retention
                    .build()
            );
            
            log.info("File '{}' with retention uploaded successfully", fileName);
            
            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(fileName);
            fileInfo.setUrl(baseUrl + "/" + bucketName + "/" + fileName);
            fileInfo.setSize(file.getSize());
            fileInfo.setContentType(file.getContentType());
            
            return fileInfo;
        } catch (Exception e) {
            log.error("Error uploading file with retention: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 8. Metrics and Monitoring
     * Add detailed metrics collection
     */
    public BucketMetrics getBucketMetrics() {
        try {
            // Get bucket statistics
            // Note: This is a simplified example. In practice, you would iterate through objects
            // to calculate total size and other metrics
            
            BucketMetrics metrics = new BucketMetrics();
            metrics.setBucketName(bucketName);
            
            // Get a sample object's info to demonstrate
            List<FileInfo> files = listFiles();
            if (!files.isEmpty()) {
                // Use the first file as an example
                FileInfo sampleFile = files.get(0);
                metrics.setSize(sampleFile.getSize());
                // In a real implementation, you would calculate actual metrics
            }
            
            metrics.setLastModified(ZonedDateTime.now());
            
            return metrics;
        } catch (Exception e) {
            log.error("Error getting bucket metrics: {}", e.getMessage());
            return null;
        }
    }
}