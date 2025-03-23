package com.example.jobhub.entity

import java.time.LocalDateTime

data class Company(
    val companyId: Int,
    val user: User,
    val companyName: String,
    val description: String?,
    val address: String?,
    val logoUrl: String?,
    val website: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val jobs: Set<Job> = emptySet(),
)
