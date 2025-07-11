package com.example.examine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // ✅ 아래 경로들 제외하고 나머지는 모두 index.html로 포워딩
        registry.addViewController("/{spring:[^\\.]+}")  // 확장자 없는 단일 경로
                .setViewName("forward:/index.html");

        registry.addViewController("/{spring:^(?!api$).*$}/**/{spring:[^\\.]+}")  // 다중 경로도 포워딩
                .setViewName("forward:/index.html");
    }
}

