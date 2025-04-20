package com.example.jobhub.service

import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.dto.StatusApplicantDTO
import com.example.jobhub.model.ApiResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApplicationService {

    @POST("api/applications")
    fun applyForJob(
        @Header("token") token: String,
        @Body applicationDTO: ApplicationDTO
    ): Call<ApiResponse<ApplicationDTO>>

    @Multipart
    @POST("api/applications/upload-resume")
    fun uploadResume(
        @Header("token") token: String,
        @Part file: MultipartBody.Part
    ): Call<ApiResponse<String>>

    @GET("api/applications/user/{userId}")
    fun getApplicationsByUserId(
        @Header("token") token: String,
        @Path("userId") userId: Int
    ): Call<ApiResponse<List<ApplicationDTO>>>

    @GET("api/applications/employer/{employerId}")
    fun getApplicationsByEmployerId(
        @Header("token") token: String,
        @Path("employerId") employerId: Int
    ): Call<ApiResponse<List<ApplicationDTO>>>

    @PUT("api/applications/status")
    fun updateApplicationStatus(
        @Header("token") token: String,
        @Body applicationDTO: ApplicationDTO
    ): Call<ApiResponse<ApplicationDTO>>

    @POST("api/applications/employer/update-status-applicant")
    fun updateStatusApplication(
        @Header("token") token: String,
        @Body statusApplicantDTO: StatusApplicantDTO
    ): Call<ApiResponse<Void>>
}