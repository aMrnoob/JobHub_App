package com.example.jobhub.dto

import java.time.LocalDateTime

data class ResumeDTO(
    val resumeId: Int? = null,
    val applicationId: Int,
    val resumeUrl: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null
)

