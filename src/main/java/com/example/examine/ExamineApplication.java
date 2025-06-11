package com.example.examine;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.examine.repository")
@EntityScan(basePackages = "com.example.examine.entity")
public class
	ExamineApplication {
	public static void main(String[] args) {
		// ✅ .env 파일 로드
		Dotenv dotenv = Dotenv.load();

		// ✅ 시스템 환경변수 등록
		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_USER", dotenv.get("DB_USER"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		SpringApplication.run(ExamineApplication.class, args);
	}
}
