package com.example.jobhub.service

import com.example.jobhub.dto.employer.JobInfo
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface JobService {
    @POST("api/job/create-job")
    fun createJob(@Body jobInfo: JobInfo): Call<ApiResponse<Void>>
}