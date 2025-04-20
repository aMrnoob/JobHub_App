package com.example.jobhub.service

import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.dto.ResumeDTO
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

    @GET("applications/company/{companyId}")
    fun getApplicationsByCompanyId(
        @Header("Authorization") token: String,
        @Path("companyId") companyId: Int
    ): Call<ApiResponse<List<ApplicationDTO>>>

    @GET("api/applications/employer/{employerId}")
    fun getApplicationsByEmployerId(
        @Header("token") token: String,
        @Path("employerId") employerId: Int
    ): Call<ApiResponse<List<ApplicationDTO>>>

    @GET("api/applications/job/{jobId}")
    fun getApplicationsByJobId(
        @Header("token") token: String,
        @Path("jobId") jobId: Int
    ): Call<ApiResponse<List<ApplicationDTO>>>

    @PUT("api/applications/status")
    fun updateApplicationStatus(
        @Header("token") token: String,
        @Body applicationDTO: ApplicationDTO
    ): Call<ApiResponse<ApplicationDTO>>

    @GET("api/applications/{applicationId}")
    fun getApplicationById(
        @Header("token") token: String,
        @Path("applicationId") applicationId: Int
    ): Call<ApiResponse<ApplicationDTO>>

    @GET("api/resumes/application/{applicationId}")
    fun getResumeByApplicationId(
        @Header("token") token: String,
        @Path("applicationId") applicationId: Int
    ): Call<ApiResponse<ResumeDTO>>

    @DELETE("api/applications/{applicationId}")
    fun deleteApplication(
        @Header("token") token: String,
        @Path("applicationId") applicationId: Int
    ): Call<ApiResponse<Boolean>>

    @GET("api/applications/stats/user/{userId}")
    fun getUserApplicationStats(
        @Header("token") token: String,
        @Path("userId") userId: Int
    ): Call<ApiResponse<Map<String, Int>>>

    @GET("api/applications/stats/employer/{employerId}")
    fun getEmployerApplicationStats(
        @Header("token") token: String,
        @Path("employerId") employerId: Int
    ): Call<ApiResponse<Map<String, Int>>>

    @POST("api/applications/employer/update-status-applicant")
    fun updateStatusApplication(
        @Header("token") token: String,
        @Body statusApplicantDTO: StatusApplicantDTO
    ): Call<ApiResponse<Void>>
}