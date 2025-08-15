package com.example.miniodemo.controller;

import com.example.miniodemo.dto.FileInfo;
import com.example.miniodemo.service.MinioService;
import com.example.miniodemo.service.PresignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;
    private final PresignedUrlService presignedUrlService;

    @PostMapping("/upload")
    public ResponseEntity<FileInfo> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        FileInfo fileInfo = minioService.uploadFile(file);
        if (fileInfo != null) {
            return ResponseEntity.ok(fileInfo);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Downloads a file by proxying it through the server.
     * Note: For better performance and scalability, consider using presigned URLs.
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        try (InputStream inputStream = minioService.downloadFile(fileName)) {
            if (inputStream == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] bytes = inputStream.readAllBytes();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets a presigned URL for downloading a file directly from MinIO.
     */
    @GetMapping("/presigned-download-url/{fileName}")
    public ResponseEntity<String> getPresignedDownloadUrl(@PathVariable String fileName) {
        String url = presignedUrlService.getPresignedDownloadUrl(fileName, 10); // 10 minutes expiry
        if (url != null) {
            return ResponseEntity.ok(url);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        minioService.deleteFile(fileName);
        return ResponseEntity.ok("File deleted successfully");
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileInfo>> listFiles() {
        List<FileInfo> files = minioService.listFiles();
        return ResponseEntity.ok(files);
    }
}