package com.example.jobhub.entity

import java.time.LocalDateTime

data class Resume(
    val resumeId: Int,
    val resumeUrl: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val application: Application?
)