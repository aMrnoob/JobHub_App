package com.example.befindingjob.controller.employer;

import com.example.befindingjob.dto.ItemJobDTO;
import com.example.befindingjob.dto.JobDTO;
import com.example.befindingjob.entity.Job;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ApiResponse<List<ItemJobDTO>> getAllJobsByUser(@RequestHeader("token") String token,
        @RequestParam(defaultValue = "0") int page) {
        return jobService.getAllJobsByUser(token, page);
    }
}
