package com.example.befindingjob.service;

import com.example.befindingjob.dto.employer.JobInfo;
import com.example.befindingjob.model.ApiResponse;

public interface JobService {
    ApiResponse<Void> createJob(JobInfo jobInfo);
}
