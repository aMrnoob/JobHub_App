package com.example.jobhub.service

import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.dto.JobDTO
import com.example.jobhub.entity.Job
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface JobService {
    @POST("api/job/create-job")
    fun createJob(@Body jobDTO: JobDTO): Call<ApiResponse<Void>>

    @POST("api/job/update-job")
    fun updateJob(@Body job: Job): Call<ApiResponse<Void>>

    @POST("api/job/delete-job")
    fun deleteJob(@Body jobId: Int): Call<ApiResponse<Void>>

    @POST("api/job/get-all-jobs-by-user")
    fun getAllJobsByUser(@Header("token") token: String): Call<ApiResponse<List<ItemJobDTO>>>

    @GET("api/job/{id}")
    fun getJobById(
        @Header("token") token: String,
        @Path("id") id: Int
    ): Call<ApiResponse<JobDTO>>
}