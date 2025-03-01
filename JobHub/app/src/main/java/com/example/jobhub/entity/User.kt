package com.example.jobhub.entity

import com.example.jobhub.entity.enumm.Role
import java.time.LocalDateTime

data class User(
    val userId: Int?,
    val email: String,
    val password: String,
    val role: Role,
    val phone: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val companies: Set<Company> = emptySet(),
    val skills: Set<Skill> = emptySet(),
    val applications: Set<Application> = emptySet()
)
