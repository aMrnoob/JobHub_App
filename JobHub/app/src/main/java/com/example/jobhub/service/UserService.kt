package com.example.jobhub.service

import com.example.jobhub.dto.UserDTO
import com.example.jobhub.dto.auth.ForgetPwdRequest
import com.example.jobhub.dto.auth.LoginRequest
import com.example.jobhub.dto.auth.LoginResponse
import com.example.jobhub.dto.auth.OtpVerifyRequest
import com.example.jobhub.dto.auth.Register_ResetPwdRequest
import com.example.jobhub.entity.User
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface UserService {
    @POST("api/auth/register")
    fun register(@Body registerRequest: Register_ResetPwdRequest): Call<ApiResponse<Void>>

    @POST("api/auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<ApiResponse<LoginResponse>>

    @POST("api/auth/request")
    fun resetPasswordRequest(@Body forgetPwdRequest: ForgetPwdRequest): Call<ApiResponse<Void>>

    @POST("api/auth/verify")
    fun verifyOtp(@Body otpVerifyRequest: OtpVerifyRequest): Call<ApiResponse<Void>>

    @POST("api/auth/reset")
    fun passwordReset(@Body resetPwdRequest: Register_ResetPwdRequest): Call<ApiResponse<Void>>

    @POST("api/admin/get-user-info")
    fun getUserInfo(@Header("token") token: String): Call<ApiResponse<User>>

    @POST("api/admin/update-user")
    fun updateUser(@Body user: User): Call<ApiResponse<Void>>
}