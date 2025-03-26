package com.example.befindingjob.service;

import com.example.befindingjob.dto.employer.JobInfo;
import com.example.befindingjob.model.ApiResponse;

import java.util.List;

public interface JobService {
    ApiResponse<Void> createJob(JobInfo jobInfo);
    ApiResponse<Void> updateJob(JobInfo jobInfo);
    ApiResponse<List<JobInfo>> getAllJobsByUser(String token);
}
