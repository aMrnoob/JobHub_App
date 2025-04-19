package com.example.befindingjob.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Data
public class FileStorageProperties {
    @Value("${upload.resume.dir}")
    private String resumeUploadPath;

    @PostConstruct
    public void init() {
        File uploadDir = new File(resumeUploadPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                System.out.println("Created directory: " + uploadDir.getAbsolutePath());
            } else {
                System.err.println("Failed to create directory: " + uploadDir.getAbsolutePath());
            }
        }
    }
}
