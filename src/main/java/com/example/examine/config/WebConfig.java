package com.example.examine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 모든 /detail/*.html 요청을 detail.html로 포워딩
        registry.addViewController("/detail/{name}.html").setViewName("forward:/detail.html");
    }
}
