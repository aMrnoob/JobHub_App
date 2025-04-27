package com.example.befindingjob.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebSecurity {
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            /*@Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://192.168.1.8:8080", "http://10.0.2.2:8080", "http://172.24.208.1:8080")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowCredentials(true);
            }*/

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String resumePath = Paths.get("./uploads/resumes/").toAbsolutePath().toUri().toString();
                registry.addResourceHandler("/uploads/resumes/**")
                        .addResourceLocations(resumePath);
            }
        };
    }
}