package com.example.jobhub.dto

import com.example.jobhub.entity.enumm.ApplicationStatus
import java.time.LocalDateTime

data class StatusApplicantDTO (
    var applicationId: Int? = 0,
    var status: ApplicationStatus? = ApplicationStatus.APPLIED,
    var message: String? = "",
    var interviewDate: LocalDateTime? = LocalDateTime.now()
)