package com.example.jobhub.dto.auth

data class OtpVerifyRequest(
    val email: String,
    val otp: String
)
