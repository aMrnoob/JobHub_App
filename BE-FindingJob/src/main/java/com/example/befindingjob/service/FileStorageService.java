package com.example.befindingjob.service;

import io.opencensus.resource.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file, String directory);
    Resource loadFileAsResource(String filePath);
}
