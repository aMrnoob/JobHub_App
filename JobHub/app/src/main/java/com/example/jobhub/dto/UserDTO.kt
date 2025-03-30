package com.example.jobhub.dto

import com.example.jobhub.entity.enumm.Role
import java.time.LocalDateTime

data class UserDTO(
    var userId: Int = 0,
    var fullname: String = "",
    var email: String = "",
    var address: String = "",
    var dateOfBirth: LocalDateTime? = null,
    var phone: String = "",
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var role: Role? = null,
    var companyIds: Set<Int> = emptySet(),
    var skillIds: Set<Int> = emptySet(),
    var applicationIds: Set<Int> = emptySet()
)
