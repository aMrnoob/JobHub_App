package com.example.jobhub.config

import com.example.jobhub.dto.auth.LoginResponse
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/auth/google")
    fun sendGoogleToken(@Body request: TokenRequest): Call<ApiResponse<LoginResponse>>
}

data class TokenRequest(
    val idToken: String
)

