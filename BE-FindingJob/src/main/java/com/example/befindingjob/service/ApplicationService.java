package com.example.befindingjob.service;

import com.example.befindingjob.dto.ApplicationDTO;
import com.example.befindingjob.dto.ResumeDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ApplicationService {
    ApplicationDTO apply(ApplicationDTO dto, String token);
    ResumeDTO saveResume(ResumeDTO dto, String token);
    String uploadResume(MultipartFile file, String token);

}
