package com.example.jobhub.dto.auth

import com.example.jobhub.entity.enumm.Role

data class LoginResponse(
    val token: String,
    val role: Role
)
