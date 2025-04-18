package com.example.befindingjob.service.impl;

import com.example.befindingjob.dto.ItemJobDTO;
import com.example.befindingjob.dto.JobDTO;
import com.example.befindingjob.entity.Company;
import com.example.befindingjob.entity.Job;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.entity.enumm.Role;
import com.example.befindingjob.mapper.JobMapper;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.repository.CompanyRepository;
import com.example.befindingjob.repository.JobRepository;
import com.example.befindingjob.repository.UserRepository;
import com.example.befindingjob.service.JobService;
import com.example.befindingjob.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JobMapper jobMapper;

    @Override
    public ApiResponse<Void> createJob(JobDTO jobDTO) {
        Optional<Company> companyOpt = companyRepository.findByCompanyName(jobDTO.getCompanyName());
        if (companyOpt.isEmpty()) {
            return new ApiResponse<>(false, "Company not found!", null);
        }

        try {
            Job job = jobMapper.JobDTOtoJob(jobDTO);
            job.setCompany(companyOpt.get());
            jobRepository.save(job);
            return new ApiResponse<>(true, "Job created successfully!");
        } catch (Exception e) {
            return new ApiResponse<>(false, "", null);
        }
    }

    @Override
    public ApiResponse<Void> updateJob(Job job) {
        Optional<Job> existingJobOpt = jobRepository.findById(job.getJobId());

        Job existingJob = existingJobOpt.get();

        if (job.getTitle() != null) existingJob.setTitle(job.getTitle());
        if (job.getDescription() != null) existingJob.setDescription(job.getDescription());
        if (job.getRequirements() != null) existingJob.setRequirements(job.getRequirements());
        if (job.getSalary() != null) existingJob.setSalary(job.getSalary());
        if (job.getLocation() != null) existingJob.setLocation(job.getLocation());
        if (job.getPostingDate() != null) existingJob.setPostingDate(job.getPostingDate());
        if (job.getExpirationDate() != null) existingJob.setExpirationDate(job.getExpirationDate());
        if (job.getExperienceRequired() != null) existingJob.setExperienceRequired(job.getExperienceRequired());
        if (job.getJobType() != null) existingJob.setJobType(job.getJobType());
        if (job.getCompany() != null) existingJob.setCompany(job.getCompany());

        jobRepository.save(existingJob);
        return new ApiResponse<>(true, "Job updated successfully.");
    }

    @Override
    public ApiResponse<Void> deleteJob(int jobId) {
        Optional<Job> jobOpt = jobRepository.findById(jobId);

        if (jobOpt.isEmpty()) {
            return new ApiResponse<>(false, "Job does not exist" + jobId, null);
        }

        jobRepository.deleteById(jobId);
        return new ApiResponse<>(true, "Delete job successfully!", null);
    }


    @Override
    public ApiResponse<List<ItemJobDTO>> getAllJobsByUser(String token) {
        if (!jwtService.isValidToken(token)) {
            return new ApiResponse<>(false, "", null);
        }

        Integer userId = jwtService.extractUserId(token);
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(false, "", null);
        }

        User user = userOpt.get();
        Role role = user.getRole();
        List<ItemJobDTO> jobs;

        if (role == Role.EMPLOYER) {
            jobs = user.getCompanies().stream()
                    .flatMap(company -> jobRepository.findByCompany(company).stream())
                    .map(ItemJobDTO::new)
                    .collect(Collectors.toList());
        } else if (role == Role.JOB_SEEKER) {
            jobs = jobRepository.findAll().stream()
                    .map(ItemJobDTO::new)
                    .collect(Collectors.toList());
        } else {
            return new ApiResponse<>(false, "User role not supported", null);
        }

        return new ApiResponse<>(true, "", jobs);
    }
}
