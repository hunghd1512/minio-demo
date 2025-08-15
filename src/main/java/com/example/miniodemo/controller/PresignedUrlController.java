package com.example.miniodemo.controller;

import com.example.miniodemo.dto.PresignedUrlResponse;
import com.example.miniodemo.service.PresignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/presigned")
@RequiredArgsConstructor
public class PresignedUrlController {

    private final PresignedUrlService presignedUrlService;

    /**
     * Generates a presigned URL for uploading an object.
     *
     * @param objectName The name of the object to upload
     * @param expiryTime The time in minutes after which the URL will expire (default: 10 minutes)
     * @return Presigned URL for uploading
     */
    @PostMapping("/upload-url")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUploadUrl(
            @RequestParam String objectName,
            @RequestParam(defaultValue = "10") int expiryTime) {
        
        String url = presignedUrlService.getPresignedUploadUrl(objectName, expiryTime);
        
        if (url != null) {
            PresignedUrlResponse response = new PresignedUrlResponse();
            response.setUrl(url);
            response.setMethod("PUT");
            response.setObjectName(objectName);
            response.setExpiryTime((long) expiryTime);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generates a presigned URL for downloading an object.
     *
     * @param objectName The name of the object to download
     * @param expiryTime The time in minutes after which the URL will expire (default: 10 minutes)
     * @return Presigned URL for downloading
     */
    @GetMapping("/download-url/{objectName}")
    public ResponseEntity<PresignedUrlResponse> generatePresignedDownloadUrl(
            @PathVariable String objectName,
            @RequestParam(defaultValue = "10") int expiryTime) {
        
        String url = presignedUrlService.getPresignedDownloadUrl(objectName, expiryTime);
        
        if (url != null) {
            PresignedUrlResponse response = new PresignedUrlResponse();
            response.setUrl(url);
            response.setMethod("GET");
            response.setObjectName(objectName);
            response.setExpiryTime((long) expiryTime);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }
}