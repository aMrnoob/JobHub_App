package com.example.befindingjob.service.impl;

import com.example.befindingjob.dto.ApplicationDTO;
import com.example.befindingjob.dto.ResumeDTO;
import com.example.befindingjob.dto.UserDTO;
import com.example.befindingjob.entity.Application;
import com.example.befindingjob.entity.Job;
import com.example.befindingjob.entity.Resume;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.repository.ApplicationRepository;
import com.example.befindingjob.repository.JobRepository;
import com.example.befindingjob.repository.ResumeRepository;
import com.example.befindingjob.repository.UserRepository;
import com.example.befindingjob.service.ApplicationService;
import com.example.befindingjob.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepo;

    @Autowired
    private ResumeRepository resumeRepo;

    @Autowired
    private JobRepository jobRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtService jwtService;

    @Value("${upload.resume.dir}")
    private String resumeUploadDir;

    private int extractUserIdFromToken(String token) {
        if (!jwtService.isValidToken(token)) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }
        return jwtService.extractUserId(token);
    }

    @Override
    public ApplicationDTO apply(ApplicationDTO dto, String token) {
        Integer jobId = dto.getJobDTO().getJobId();
        Integer userId = extractUserIdFromToken(token);

        Job job = jobRepo.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Application application = new Application();
        application.setJob(job);
        application.setUser(user);
        application.setCoverLetter(dto.getCoverLetter());
        application.setStatus(dto.getStatus());
        application.setApplicationDate(LocalDateTime.now());

        application = applicationRepo.save(application);

        dto.setApplicationId(application.getApplicationId());
        dto.setApplicationDate(application.getApplicationDate());

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setFullName(user.getFullname());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());
        userDTO.setImageUrl(user.getImageUrl());

        dto.setUserDTO(userDTO);

        return dto;
    }

    @Override
    public ResumeDTO saveResume(ResumeDTO dto, String token) {
        Application app = applicationRepo.findById(dto.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn ứng tuyển"));

        Resume resume = new Resume();
        resume.setApplication(app);
        resume.setResumeUrl(dto.getResumeUrl());
        resume.setCreatedAt(LocalDateTime.now());
        resume.setUpdatedAt(LocalDateTime.now());

        resume = resumeRepo.save(resume);

        dto.setResumeId(resume.getResumeId());
        dto.setCreatedAt(resume.getCreatedAt());
        dto.setUpdatedAt(resume.getUpdatedAt());

        return dto;
    }

    @Override
    public String uploadResume(MultipartFile file, String token) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String filePath = Paths.get(resumeUploadDir, fileName).toString();

            file.transferTo(new File(filePath));

            return "/uploads/resumes/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Tải lên file thất bại", e);
        }
    }
}
