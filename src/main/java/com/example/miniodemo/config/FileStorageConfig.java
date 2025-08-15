//package com.example.miniodemo.config;
//
//import io.minio.MinioClient;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class FileStorageConfig {
//    @Value("${minio.baseUrl}")
//    private String minioBaseUrl;
//    @Value("${minio.accessKey}")
//    private String minioAccessKey;
//    @Value("${minio.secretKey}")
//    private String minioSecretKey;
//
//    public FileStorageConfig() {
//    }
//
//    @Bean
//    public MinioClient minioClient() {
//        return MinioClient.builder().endpoint(this.minioBaseUrl).credentials(this.minioAccessKey, this.minioSecretKey).build();
//    }
//}
