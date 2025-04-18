package com.example.befindingjob.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebSecurity {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
<<<<<<< .mine
                        .allowedOrigins("http://172.172.12.185:8080", "http://10.0.2.2:8080")
=======
                        .allowedOrigins("http://192.168.1.21:8080", "http://10.0.2.2:8080")
>>>>>>> .theirs
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowCredentials(true);
            }
        };
    }
}
