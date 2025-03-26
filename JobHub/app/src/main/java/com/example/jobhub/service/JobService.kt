package com.example.jobhub.service

import com.example.jobhub.dto.employer.JobInfo
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface JobService {
    @POST("api/job/create-job")
    fun createJob(@Body jobInfo: JobInfo): Call<ApiResponse<Void>>

    @POST("api/job/update-job")
    fun updateJob(@Body jobInfo: JobInfo): Call<ApiResponse<List<JobInfo>>>

    @POST("api/job/get-all-jobs-by-user")
    fun getAllJobsByUser(@Header("token") token: String): Call<ApiResponse<List<JobInfo>>>
}