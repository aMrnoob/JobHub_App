package com.example.befindingjob.service.impl;

import com.example.befindingjob.service.FileStorageService;
import io.opencensus.resource.Resource;
import org.springframework.web.multipart.MultipartFile;

public class FileStorageServiceImpl implements FileStorageService {
    @Override
    public String storeFile(MultipartFile file, String directory) {
        return "";
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        return null;
    }
}
