package com.example.befindingjob.service;

import com.example.befindingjob.dto.ItemJobDTO;
import com.example.befindingjob.dto.JobDTO;
import com.example.befindingjob.entity.Job;
import com.example.befindingjob.model.ApiResponse;

import java.util.List;

public interface JobService {
    ApiResponse<Void> createJob(JobDTO jobDTO);
    ApiResponse<Void> updateJob(Job job);
    ApiResponse<List<ItemJobDTO>> getAllJobsByUser(String token);
}
