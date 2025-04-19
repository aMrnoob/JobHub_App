package com.example.jobhub.dto

import com.example.jobhub.entity.enumm.ApplicationStatus
import java.time.LocalDateTime

data class ApplicationDTO (
    val applicationId: Int? = null,
    val jobDTO: ItemJobDTO,
    val userDTO: UserDTO,
    val coverLetter: String,
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    val applicationDate: LocalDateTime = LocalDateTime.now()
)