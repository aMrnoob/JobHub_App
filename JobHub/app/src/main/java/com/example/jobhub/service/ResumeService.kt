package com.example.jobhub.service

import com.example.jobhub.dto.ResumeDTO
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.*

interface ResumeService {
    @POST("api/applications/resumes")
    fun createResume(
        @Header("token") token: String,
        @Body resumeDTO: ResumeDTO
    ): Call<ApiResponse<ResumeDTO>>

    @GET("api/applications/resumes/{applicationId}")
    fun getResumeByApplicationId(
        @Header("token") token: String,
        @Path("applicationId") applicationId: Int
    ): Call<ApiResponse<ResumeDTO>>
}