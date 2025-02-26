package com.example.jobhub.model

data class ApiResponse<T>(
    val isSuccess: Boolean,
    val message: String? = null,
    val data: T? = null
)