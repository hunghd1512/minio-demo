package com.example.miniodemo.dto;

import lombok.Data;

@Data
public class PresignedUrlResponse {
    private String url;
    private String method;
    private String objectName;
    private Long expiryTime; // Expiry time in minutes
}