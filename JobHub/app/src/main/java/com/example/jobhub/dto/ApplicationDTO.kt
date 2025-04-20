package com.example.jobhub.dto

import com.example.jobhub.entity.enumm.ApplicationStatus
import java.time.LocalDateTime

data class ApplicationDTO (
    val applicationId: Int? = null,
    val jobDTO: ItemJobDTO,
    val userDTO: UserDTO,
    var coverLetter: String,
    val resumeUrl: String,
    var status: ApplicationStatus = ApplicationStatus.APPLIED,
    val applicationDate: LocalDateTime = LocalDateTime.now(),
    var interviewDate: LocalDateTime? = LocalDateTime.now()
)