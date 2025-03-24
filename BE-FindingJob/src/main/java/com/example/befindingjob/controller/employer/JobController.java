package com.example.befindingjob.controller.employer;

import com.example.befindingjob.dto.employer.JobInfo;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping("/create-job")
    public ApiResponse<Void> createJob(@RequestBody JobInfo jobInfo) {
        return jobService.createJob(jobInfo);
    }
}
