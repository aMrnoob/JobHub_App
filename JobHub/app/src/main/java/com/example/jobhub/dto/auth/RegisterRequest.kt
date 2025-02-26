package com.example.jobhub.dto.auth

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)
