package com.example.jobhub.dto

import com.example.jobhub.entity.enumm.Role

data class UserDTO (
    val userId: Int? = null,
    var fullName: String = "",
    val email: String = "",
    val password: String = "",
    var role: Role = Role.UNDEFINED,
    var address: String? = null,
    var dateOfBirth: String = "",
    var phone: String? = null,
    var imageUrl: String? = null,
    val createdAt: String = "",
    val companyId: Int? = null,
    val updatedAt: String = ""
)