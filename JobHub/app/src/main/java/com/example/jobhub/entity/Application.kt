package com.example.jobhub.entity

import com.example.jobhub.entity.enumm.ApplicationStatus
import java.time.LocalDateTime

data class Application(
    val applicationId: Int,
    val job: Job,
    val user: User,
    val applicationDate: LocalDateTime?,
    val status: ApplicationStatus?,
    val coverLetter: String?,
    val resume: Resume?
)
