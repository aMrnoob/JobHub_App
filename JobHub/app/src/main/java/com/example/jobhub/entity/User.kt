package com.example.jobhub.entity

import android.os.Parcelable
import com.example.jobhub.entity.enumm.Role
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class User(
    val userId: Int? = null,
    var fullName: String = "",
    val email: String = "",
    val password: String = "",
    var role: Role = Role.UNDEFINED,
    var address: String? = null,
    var dateOfBirth: LocalDateTime? = null,
    var phone: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,
    val companies: Set<Company> = emptySet(),
    val skills: Set<Skill> = emptySet(),
    val applications: Set<Application> = emptySet()
) : Parcelable
