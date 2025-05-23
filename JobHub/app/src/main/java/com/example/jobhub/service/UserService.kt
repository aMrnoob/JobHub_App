package com.example.jobhub.service

import com.example.jobhub.dto.UserDTO
import com.example.jobhub.dto.auth.OtpRequest
import com.example.jobhub.dto.auth.LoginRequest
import com.example.jobhub.dto.auth.LoginResponse
import com.example.jobhub.dto.auth.OtpVerifyRequest
import com.example.jobhub.dto.auth.Register_ResetPwdRequest
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {
    @POST("api/auth/register")
    fun register(@Body registerRequest: Register_ResetPwdRequest): Call<ApiResponse<Void>>

    @POST("api/auth/otp-register")
    fun otpRegister(@Body otpRequest: OtpRequest): Call<ApiResponse<Void>>

    @POST("api/auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<ApiResponse<LoginResponse>>

    @POST("api/auth/request")
    fun resetPasswordRequest(@Body otpRequest: OtpRequest): Call<ApiResponse<Void>>

    @POST("api/auth/verify")
    fun verifyOtp(@Body otpVerifyRequest: OtpVerifyRequest): Call<ApiResponse<Void>>

    @POST("api/auth/reset")
    fun passwordReset(@Body resetPwdRequest: Register_ResetPwdRequest): Call<ApiResponse<Void>>

    @POST("api/users/update-user")
    fun updateUser(@Body userDTO: UserDTO): Call<ApiResponse<Void>>

    @GET("api/users/me")
    fun getUser(@Header("token") token: String): Call<ApiResponse<UserDTO>>

    @GET("api/users/find-by-email")
    fun findByEmail(@Header("email") email: String): Call<ApiResponse<UserDTO>>

    @DELETE("api/users/delete-account")
    fun deleteAccount(@Header("token") token: String): Call<ApiResponse<UserDTO>>

}