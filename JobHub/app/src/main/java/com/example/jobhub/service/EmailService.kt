package com.example.jobhub.service

import com.example.jobhub.dto.EmailRequestDTO
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface EmailService {
    @POST("api/email/send")
    fun sendEmail(
        @Header("token") token: String,
        @Body emailRequest: EmailRequestDTO
    ): Call<ApiResponse<Boolean>>
}