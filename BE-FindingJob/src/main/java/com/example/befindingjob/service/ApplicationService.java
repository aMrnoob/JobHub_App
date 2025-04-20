package com.example.befindingjob.service;

import com.example.befindingjob.dto.ApplicationDTO;
import com.example.befindingjob.dto.ResumeDTO;
import com.example.befindingjob.dto.StatusApplicantDTO;
import com.example.befindingjob.model.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ApplicationService {

    ApiResponse<ApplicationDTO> applyForJob(String token, ApplicationDTO applicationDTO);
    ApiResponse<String> uploadResume(String token, MultipartFile file);
    ApiResponse<List<ApplicationDTO>> getApplicationsByUserId(String token, Integer userId);
    ApiResponse<List<ApplicationDTO>> getApplicationsByEmployerId(String token, Integer employerId);
    ApiResponse<List<ApplicationDTO>> getApplicationsByJobId(String token, Integer jobId);
    ApiResponse<ApplicationDTO> updateApplicationStatus(String token, ApplicationDTO applicationDTO);
    ApiResponse<ApplicationDTO> getApplicationById(String token, Integer applicationId);
    ApiResponse<ResumeDTO> getResumeByApplicationId(String token, Integer applicationId);
    ApiResponse<Boolean> deleteApplication(String token, Integer applicationId);
    ApiResponse<Map<String, Integer>> getUserApplicationStats(String token, Integer userId);
    ApiResponse<Map<String, Integer>> getEmployerApplicationStats(String token, Integer employerId);
    ApiResponse<Void> updateStatusApplication(String token, StatusApplicantDTO statusApplicantDTO);
}
