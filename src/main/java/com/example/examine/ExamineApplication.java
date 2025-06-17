package com.example.examine;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.examine.repository")
@EntityScan(basePackages = "com.example.examine.entity")
public class ExamineApplication {
	public static void main(String[] args) {
		// ✅ .env 로드
		Dotenv dotenv = Dotenv.load();

		// ✅ Spring에 환경 변수로 주입
		new SpringApplicationBuilder(ExamineApplication.class)
				.properties(
						"DB_URL=" + dotenv.get("DB_URL"),
						"DB_USER=" + dotenv.get("DB_USER"),
						"DB_PASSWORD=" + dotenv.get("DB_PASSWORD")
				)
				.run(args);
	}
}
