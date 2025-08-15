package com.example.miniodemo.dto;

import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class BucketMetrics {
    private String bucketName;
    private ZonedDateTime lastModified;
    private Long size;
}