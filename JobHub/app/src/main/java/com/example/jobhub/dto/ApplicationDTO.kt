package com.example.jobhub.dto

import com.example.jobhub.entity.enumm.ApplicationStatus
import java.util.Date

data class ApplicationDTO (
    val applicationId: Int? = null,
    val jobDTO: ItemJobDTO,
    val userDTO: UserDTO,
    val coverLetter: String,
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    val applicationDate: Date? = null,
    val cvUrl: String? = null
)