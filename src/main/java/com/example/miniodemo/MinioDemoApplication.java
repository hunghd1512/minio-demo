package com.example.miniodemo;

import com.example.miniodemo.service.MinioService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class MinioDemoApplication {

	private final MinioService minioService;

	public static void main(String[] args) {
		SpringApplication.run(MinioDemoApplication.class, args);
	}

	@PostConstruct
	public void init() {
		minioService.createBucket();
	}

}
