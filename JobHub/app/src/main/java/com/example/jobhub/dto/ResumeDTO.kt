package com.example.jobhub.dto

import java.util.Date

data class ResumeDTO(
    val resumeId: Int? = null,
    val applicationId: Int,
    val resumeUrl: String,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)

