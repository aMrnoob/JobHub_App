package com.example.befindingjob.controller.employer;

import com.example.befindingjob.dto.ApplicationDTO;
import com.example.befindingjob.dto.ResumeDTO;
import com.example.befindingjob.dto.StatusApplicantDTO;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationDTO>> applyForJob(
            @RequestHeader("token") String token,
            @RequestBody ApplicationDTO applicationDTO) {
        ApiResponse<ApplicationDTO> response = applicationService.applyForJob(token, applicationDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-resume")
    public ResponseEntity<ApiResponse<String>> uploadResume(
            @RequestHeader("token") String token,
            @RequestParam("file") MultipartFile file) {
        ApiResponse<String> response = applicationService.uploadResume(token, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ApplicationDTO>>> getApplicationsByUserId(
            @RequestHeader("token") String token,
            @PathVariable Integer userId) {
        ApiResponse<List<ApplicationDTO>> response = applicationService.getApplicationsByUserId(token, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employer/{employerId}")
    public ResponseEntity<ApiResponse<List<ApplicationDTO>>> getApplicationsByEmployerId(
            @RequestHeader("token") String token,
            @PathVariable Integer employerId) {
        ApiResponse<List<ApplicationDTO>> response = applicationService.getApplicationsByEmployerId(token, employerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<ApplicationDTO>>> getApplicationsByJobId(
            @RequestHeader("token") String token,
            @PathVariable Integer jobId) {
        ApiResponse<List<ApplicationDTO>> response = applicationService.getApplicationsByJobId(token, jobId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/status")
    public ResponseEntity<ApiResponse<ApplicationDTO>> updateApplicationStatus(
            @RequestHeader("token") String token,
            @RequestBody ApplicationDTO applicationDTO) {
        ApiResponse<ApplicationDTO> response = applicationService.updateApplicationStatus(token, applicationDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<ApplicationDTO>> getApplicationById(
            @RequestHeader("token") String token,
            @PathVariable Integer applicationId) {
        ApiResponse<ApplicationDTO> response = applicationService.getApplicationById(token, applicationId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteApplication(
            @RequestHeader("token") String token,
            @PathVariable Integer applicationId) {
        ApiResponse<Boolean> response = applicationService.deleteApplication(token, applicationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resumes/{applicationId}")
    public ResponseEntity<ApiResponse<ResumeDTO>> getResumeByApplicationId(
            @RequestHeader("token") String token,
            @PathVariable Integer applicationId) {
        ApiResponse<ResumeDTO> response = applicationService.getResumeByApplicationId(token, applicationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getUserApplicationStats(
            @RequestHeader("token") String token,
            @PathVariable Integer userId) {
        ApiResponse<Map<String, Integer>> response = applicationService.getUserApplicationStats(token, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/employer/{employerId}")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getEmployerApplicationStats(
            @RequestHeader("token") String token,
            @PathVariable Integer employerId) {
        ApiResponse<Map<String, Integer>> response = applicationService.getEmployerApplicationStats(token, employerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/employer/update-status-applicant")
    public ApiResponse<Void> updateStatusApplicant(
            @RequestHeader("token") String token,
            @RequestBody StatusApplicantDTO statusApplicantDTO) {
       return applicationService.updateStatusApplication(token, statusApplicantDTO);
    }
}
