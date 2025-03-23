package com.example.jobhub.dto.employer

import com.example.jobhub.entity.Company
import com.example.jobhub.entity.Skill
import com.example.jobhub.entity.enumm.JobType
import java.time.LocalDateTime

data class JobInfo(
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
)
