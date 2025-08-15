package com.example.miniodemo.dto;

import lombok.Data;

@Data
public class FileInfo {
    private String name;
    private String url;
    private Long size;
    private String contentType;
}