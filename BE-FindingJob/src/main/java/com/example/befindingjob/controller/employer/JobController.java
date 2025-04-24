package com.example.befindingjob.controller.employer;

import com.example.befindingjob.dto.ItemJobDTO;
import com.example.befindingjob.dto.JobDTO;
import com.example.befindingjob.entity.Job;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.JobService;
import com.example.befindingjob.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping("/create-job")
    public ApiResponse<Void> createJob(@RequestBody JobDTO jobDTO) {
        return jobService.createJob(jobDTO);
    }

    @PostMapping("/update-job")
    public ApiResponse<Void> updateJob(@RequestBody Job job) {
        return jobService.updateJob(job);
    }

    @PostMapping("/delete-job")
    public ApiResponse<Void> deleteJob(@RequestBody int jobId) {
        return jobService.deleteJob(jobId);
    }

    @PostMapping("/get-all-jobs-by-user")
    public ApiResponse<List<ItemJobDTO>> getAllJobsByUser(@RequestHeader("token") String token) {
        return jobService.getAllJobsByUser(token);
    }

}
