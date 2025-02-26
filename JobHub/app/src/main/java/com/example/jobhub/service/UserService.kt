package com.example.jobhub.service

import com.example.jobhub.dto.auth.LoginRequest
import com.example.jobhub.dto.auth.LoginResponse
import com.example.jobhub.dto.auth.RegisterRequest
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {
    @POST("api/auth/register")
    fun register(@Body registerRequest: RegisterRequest): Call<ApiResponse<Void>>

    @POST("api/auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<ApiResponse<LoginResponse>>
}