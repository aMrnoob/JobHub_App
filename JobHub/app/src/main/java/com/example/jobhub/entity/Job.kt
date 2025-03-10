package com.example.jobhub.entity

import com.example.jobhub.entity.enumm.JobType
import java.time.LocalDateTime

data class Job(
    val jobId: Int,
    var title: String,
    val description: String,
    val requirements: String?,
    val salary: String?,
    val location: String?,
    val jobType: JobType?,
    val experienceRequired: String?,
    val postingDate: LocalDateTime?,
    val expirationDate: LocalDateTime?,
    val company: Company,
    val requiredSkills: Set<Skill> = emptySet(),
    val applications: Set<Application> = emptySet()
)