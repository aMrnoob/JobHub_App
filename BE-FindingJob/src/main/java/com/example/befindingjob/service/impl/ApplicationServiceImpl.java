package com.example.befindingjob.service.impl;

import com.example.befindingjob.config.FileStorageProperties;
import com.example.befindingjob.dto.*;
import com.example.befindingjob.entity.Application;
import com.example.befindingjob.entity.Job;
import com.example.befindingjob.entity.Resume;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.entity.enumm.ApplicationStatus;
import com.example.befindingjob.model.ApiResponse;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Autowired
    private JwtService jwtService;

    @Value("${upload.resume.dir}")
    private String resumeUploadPath;

    @Override
    public ApiResponse<ApplicationDTO> applyForJob(String token, ApplicationDTO applicationDTO) {
        try {
            Integer userId = jwtService.extractUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!Objects.equals(applicationDTO.getUserDTO().getUserId(), userId)) {
                return new ApiResponse<>(false, "You can only apply with your own account", null);
            }

            Job job = jobRepository.findById(applicationDTO.getJobDTO().getJobId())
                    .orElseThrow(() -> new RuntimeException("Job not found"));

            Optional<Application> existingApplication = applicationRepository.findByUserAndJob(user, job);
            if (existingApplication.isPresent()) {
                return new ApiResponse<>(false, "You have already applied for this job", null);
            }

            Application application = new Application();
            application.setUser(user);
            application.setJob(job);
            application.setCoverLetter(applicationDTO.getCoverLetter());
            application.setStatus(Optional.ofNullable(applicationDTO.getStatus()).orElse(ApplicationStatus.APPLIED));
            application.setApplicationDate(LocalDateTime.now());

            Application saved = applicationRepository.save(application);
            return new ApiResponse<>(true, "Application submitted successfully", convertToDTO(saved));
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to submit application: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<String> uploadResume(String token, MultipartFile file) {
        try {
            if (!jwtService.isValidToken(token)) {
                return new ApiResponse<>(false, "Invalid token", null);
            }

            Path uploadPath = Paths.get(fileStorageProperties.getUploadDir());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID() + extension;

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String resumeUrl = "/uploads/resumes/" + filename;
            return new ApiResponse<>(true, "Resume uploaded successfully", resumeUrl);
        } catch (IOException e) {
            return new ApiResponse<>(false, "Failed to upload resume: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ResumeDTO> createResume(String token, ResumeDTO resumeDTO) {
        try {
            if (!jwtService.isValidToken(token)) {
                return new ApiResponse<>(false, "Invalid token", null);
            }

            Application application = applicationRepository.findById(resumeDTO.getApplicationId())
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            Resume resume = new Resume();
            resume.setApplication(application);
            resume.setResumeUrl(resumeDTO.getResumeUrl());
            resume.setCreatedAt(LocalDateTime.now());
            resume.setUpdatedAt(LocalDateTime.now());

            Resume saved = resumeRepository.save(resume);
            ResumeDTO response = new ResumeDTO(
                    saved.getResumeId(),
                    saved.getApplication().getApplicationId(),
                    saved.getResumeUrl(),
                    saved.getCreatedAt(),
                    saved.getUpdatedAt()
            );

            return new ApiResponse<>(true, "Resume saved successfully", response);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to save resume: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<ApplicationDTO>> getApplicationsByUserId(String token, Integer userId) {
        try {
            Integer tokenUserId = jwtService.extractUserId(token);
            if (!Objects.equals(tokenUserId, userId)) {
                return new ApiResponse<>(false, "You can only access your own applications", null);
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Application> applications = applicationRepository.findByUser(user);
            List<ApplicationDTO> applicationDTOs = applications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return new ApiResponse<>(true, "Applications retrieved successfully", applicationDTOs);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to retrieve applications: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<ApplicationDTO>> getApplicationsByEmployerId(String token, Integer employerId) {
        try {
            Integer tokenUserId = jwtService.extractUserId(token);
            User user = userRepository.findById(tokenUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getRole().equals("EMPLOYER")) {
                return new ApiResponse<>(false, "Only employers can access this resource", null);
            }

            List<Job> employerJobs = jobRepository.findByEmployerId(employerId);
            if (employerJobs.isEmpty()) {
                return new ApiResponse<>(true, "No job postings found for this employer", new ArrayList<>());
            }

            List<Application> applications = new ArrayList<>();
            for (Job job : employerJobs) {
                applications.addAll(applicationRepository.findByJob(job));
            }

            List<ApplicationDTO> applicationDTOs = applications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return new ApiResponse<>(true, "Applications retrieved successfully", applicationDTOs);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to retrieve applications: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<ApplicationDTO>> getApplicationsByJobId(String token, Integer jobId) {
        try {
            if (!jwtService.isValidToken(token)) {
                return new ApiResponse<>(false, "Invalid token", null);
            }

            Integer tokenUserId = jwtService.extractUserId(token);
            User user = userRepository.findById(tokenUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));

            if (!user.getRole().equals("EMPLOYER") || !job.getCompany().getUser().getUserId().equals(tokenUserId)) {
                return new ApiResponse<>(false, "You can only access applications for your own job listings", null);
            }

            List<Application> applications = applicationRepository.findByJob(job);
            List<ApplicationDTO> applicationDTOs = applications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return new ApiResponse<>(true, "Applications retrieved successfully", applicationDTOs);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to retrieve applications: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ApplicationDTO> updateApplicationStatus(String token, ApplicationDTO applicationDTO) {
        try {
            Integer tokenUserId = jwtService.extractUserId(token);
            User user = userRepository.findById(tokenUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getRole().equals("EMPLOYER")) {
                return new ApiResponse<>(false, "Only employers can update application status", null);
            }

            Application application = applicationRepository.findById(applicationDTO.getApplicationId())
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            if (!application.getJob().getCompany().getUser().getUserId().equals(tokenUserId)) {
                return new ApiResponse<>(false, "You can only update applications for your own job listings", null);
            }

            application.setStatus(applicationDTO.getStatus());
            Application updatedApplication = applicationRepository.save(application);

            ApplicationDTO responseDTO = convertToDTO(updatedApplication);
            return new ApiResponse<>(true, "Application status updated successfully", responseDTO);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to update application status: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ApplicationDTO> getApplicationById(String token, Integer applicationId) {
        try {
            if (!jwtService.isValidToken(token)) {
                return new ApiResponse<>(false, "Invalid token", null);
            }

            Integer tokenUserId = jwtService.extractUserId(token);
            User user = userRepository.findById(tokenUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Application application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            if (!user.getRole().equals("EMPLOYER") && !application.getUser().getUserId().equals(tokenUserId)) {
                return new ApiResponse<>(false, "You can only access your own applications or applications for your job listings", null);
            }

            if (user.getRole().equals("EMPLOYER") && !application.getJob().getCompany().getUser().getUserId().equals(tokenUserId)) {
                return new ApiResponse<>(false, "You can only access applications for your own job listings", null);
            }

            ApplicationDTO responseDTO = convertToDTO(application);
            return new ApiResponse<>(true, "Application retrieved successfully", responseDTO);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to retrieve application: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<ResumeDTO> getResumeByApplicationId(String token, Integer applicationId) {
        try {
            if (!jwtService.isValidToken(token)) {
                return new ApiResponse<>(false, "Invalid token", null);
            }

            Integer tokenUserId = jwtService.extractUserId(token);
            User user = userRepository.findById(tokenUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Application application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            if (!user.getRole().equals("EMPLOYER") && !application.getUser().getUserId().equals(tokenUserId)) {
                return new ApiResponse<>(false, "You can only access resumes for your own applications or your job listings", null);
            }

            if (user.getRole().equals("EMPLOYER") && !application.getJob().getCompany().getUser().getUserId().equals(tokenUserId)) {
                return new ApiResponse<>(false, "You can only access resumes for your own job listings", null);
            }

            Resume resume = resumeRepository.findByApplicationId(applicationId)
                    .orElse(null);

            if (resume == null) {
                return new ApiResponse<>(false, "No resume found for this application", null);
            }

            ResumeDTO resumeDTO = new ResumeDTO(
                    resume.getResumeId(),
                    resume.getApplication().getApplicationId(),
                    resume.getResumeUrl(),
                    resume.getCreatedAt(),
                    resume.getUpdatedAt()
            );

            return new ApiResponse<>(true, "Resume retrieved successfully", resumeDTO);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to retrieve resume: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Boolean> deleteApplication(String token, Integer applicationId) {
        try {
            if (!jwtService.isValidToken(token)) {
                return new ApiResponse<>(false, "Invalid token", null);
            }

            Integer tokenUserId = jwtService.extractUserId(token);
            User user = userRepository.findById(tokenUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Application application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            if (!application.getUser().getUserId().equals(tokenUserId)) {
                return new ApiResponse<>(false, "You can only delete your own applications", null);
            }

            applicationRepository.delete(application);
            return new ApiResponse<>(true, "Application deleted successfully", true);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to delete application: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Map<String, Integer>> getUserApplicationStats(String token, Integer userId) {
        try {
            Integer tokenUserId = jwtService.extractUserId(token);
            if (!Objects.equals(tokenUserId, userId)) {
                return new ApiResponse<>(false, "You can only access your own stats", null);
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Application> applications = applicationRepository.findByUser(user);

            Map<String, Integer> stats = new HashMap<>();
            stats.put("APPLIED", countApplicationsByStatus(applications, ApplicationStatus.APPLIED));
            stats.put("REVIEWED", countApplicationsByStatus(applications, ApplicationStatus.REVIEWED));
            stats.put("ACCEPTED", countApplicationsByStatus(applications, ApplicationStatus.ACCEPTED));
            stats.put("REJECTED", countApplicationsByStatus(applications, ApplicationStatus.REJECTED));

            return new ApiResponse<>(true, "Application stats retrieved successfully", stats);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to retrieve application stats: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Map<String, Integer>> getEmployerApplicationStats(String token, Integer employerId) {
        try {
            Integer tokenUserId = jwtService.extractUserId(token);
            User user = userRepository.findById(tokenUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getRole().equals("EMPLOYER") || !Objects.equals(tokenUserId, employerId)) {
                return new ApiResponse<>(false, "You can only access your own employer stats", null);
            }

            List<Job> employerJobs = jobRepository.findByEmployerId(employerId);
            if (employerJobs.isEmpty()) {
                Map<String, Integer> emptyStats = new HashMap<>();
                emptyStats.put("APPLIED", 0);
                emptyStats.put("REVIEWED", 0);
                emptyStats.put("ACCEPTED", 0);
                emptyStats.put("REJECTED", 0);
                return new ApiResponse<>(true, "No job postings found for this employer", emptyStats);
            }

            List<Application> applications = new ArrayList<>();
            for (Job job : employerJobs) {
                applications.addAll(applicationRepository.findByJob(job));
            }

            Map<String, Integer> stats = new HashMap<>();
            stats.put("APPLIED", countApplicationsByStatus(applications, ApplicationStatus.APPLIED));
            stats.put("REVIEWED", countApplicationsByStatus(applications, ApplicationStatus.REVIEWED));
            stats.put("ACCEPTED", countApplicationsByStatus(applications, ApplicationStatus.ACCEPTED));
            stats.put("REJECTED", countApplicationsByStatus(applications, ApplicationStatus.REJECTED));

            return new ApiResponse<>(true, "Employer application stats retrieved successfully", stats);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to retrieve employer application stats: " + e.getMessage(), null);
        }
    }

    private int countApplicationsByStatus(List<Application> applications, ApplicationStatus status) {
        return (int) applications.stream()
                .filter(app -> app.getStatus() == status)
                .count();
    }

    private ApplicationDTO convertToDTO(Application application) {
        return new ApplicationDTO(
                application.getApplicationId(),
                convertJobToDTO(application.getJob()),
                convertUserToDTO(application.getUser()),
                application.getCoverLetter(),
                application.getStatus(),
                application.getApplicationDate()
        );
    }

    private UserDTO convertUserToDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getFullname(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getAddress(),
                (user.getDateOfBirth() != null) ? user.getDateOfBirth().toString() : null,
                user.getPhone(),
                user.getImageUrl(),
                (user.getCreatedAt() != null) ? user.getCreatedAt().toString() : null,
                (user.getUpdatedAt() != null) ? user.getUpdatedAt().toString() : null
        );
    }

    private ItemJobDTO convertJobToDTO(Job job) {
        ItemCompanyDTO companyDTO = new ItemCompanyDTO(job.getCompany());

        return new ItemJobDTO(
                job.getJobId(),
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                job.getSalary(),
                job.getLocation(),
                job.getJobType(),
                job.getExperienceRequired(),
                job.getExpirationDate(),
                companyDTO,
                job.getRequiredSkills().stream().map(SkillDTO::new).collect(Collectors.toList())
        );
    }
}