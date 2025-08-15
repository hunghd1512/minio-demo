package com.example.miniodemo.controller;

import com.example.miniodemo.dto.BucketMetrics;
import com.example.miniodemo.dto.FileInfo;
import com.example.miniodemo.service.MinioService;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import io.minio.messages.RetentionMode;
import io.minio.messages.RetentionDurationDays;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advanced")
@RequiredArgsConstructor
public class AdvancedMinioController {

    private final MinioService minioService;

    /**
     * 1. Object Lifecycle Management
     */
//    @PostMapping("/lifecycle")
//    public ResponseEntity<String> setObjectLifecycleRules() {
//        minioService.setObjectLifecycleRules();
//        return ResponseEntity.ok("Lifecycle rules set successfully");
//    }

    /**
     * 2. Object Versioning
     */
//    @PostMapping("/versioning/enable")
//    public ResponseEntity<String> enableBucketVersioning() {
//        minioService.enableBucketVersioning();
//        return ResponseEntity.ok("Versioning enabled successfully");
//    }

    @GetMapping("/versioning/list/{objectName}")
    public ResponseEntity<List<String>> listObjectVersions(@PathVariable String objectName) {
        List<String> versions = minioService.listObjectVersions(objectName);
        return ResponseEntity.ok(versions);
    }

    /**
     * 3. Server-Side Encryption
     */
    @PostMapping("/upload/encrypted")
    public ResponseEntity<FileInfo> uploadEncryptedFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        FileInfo fileInfo = minioService.uploadEncryptedFile(file);
        if (fileInfo != null) {
            return ResponseEntity.ok(fileInfo);
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 4. Object Tagging and Metadata
     */
//    @PostMapping("/upload/tagged")
//    public ResponseEntity<FileInfo> uploadFileWithTagging(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam(required = false) String tagKey,
//            @RequestParam(required = false) String tagValue) {
//
//        if (file.isEmpty()) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        Map<String, String> tags = new HashMap<>();
//        if (tagKey != null && tagValue != null) {
//            tags.put(tagKey, tagValue);
//        }
//
//        FileInfo fileInfo = minioService.uploadFileWithTagging(file, tags);
//        if (fileInfo != null) {
//            return ResponseEntity.ok(fileInfo);
//        } else {
//            return ResponseEntity.internalServerError().build();
//        }
//    }

    @GetMapping("/tags/{objectName}")
    public ResponseEntity<Map<String, String>> getObjectTags(@PathVariable String objectName) {
        Map<String, String> tags = minioService.getObjectTags(objectName);
        return ResponseEntity.ok(tags);
    }

    /**
     * 7. Object Locking (WORM)
     */
    @PostMapping("/locking/enable")
    public ResponseEntity<String> enableObjectLocking() {
        minioService.enableObjectLocking();
        return ResponseEntity.ok("Object locking enabled successfully");
    }

    @PostMapping("/upload/retention")
    public ResponseEntity<FileInfo> uploadWithRetention(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "COMPLIANCE") String retentionMode,
            @RequestParam(defaultValue = "365") int retentionDays) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        RetentionMode mode = retentionMode.equals("GOVERNANCE") ? 
            RetentionMode.GOVERNANCE : RetentionMode.COMPLIANCE;
        
        FileInfo fileInfo = minioService.uploadWithRetention(
            file, 
            mode,
                ZonedDateTime.of(0,retentionDays,0,0,0,0,0, ZoneId.of(""))
        );
        
        if (fileInfo != null) {
            return ResponseEntity.ok(fileInfo);
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 8. Metrics and Monitoring
     */
    @GetMapping("/metrics")
    public ResponseEntity<BucketMetrics> getBucketMetrics() {
        BucketMetrics metrics = minioService.getBucketMetrics();
        if (metrics != null) {
            return ResponseEntity.ok(metrics);
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }
}