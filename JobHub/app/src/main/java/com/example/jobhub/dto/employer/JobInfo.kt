package com.example.jobhub.dto.employer

import com.example.jobhub.entity.Skill
import com.example.jobhub.entity.enumm.JobType
import java.time.LocalDateTime

data class JobInfo(
    val jobId: Int? = null,
    var title: String = "",
    val companyInfo: CompanyInfo? = null,
    val description: String = "",
    val requirements: String = "",
    val salary: String = "",
    val location: String = "",
    val jobType: JobType? = null,
    val experienceRequired: String = "",
    val postingDate: LocalDateTime? = null,
    val expirationDate: LocalDateTime? = null,
    val requiredSkills: Set<Skill> = emptySet(),
    val createdAt: String = "",
    val updatedAt: String = "",
)
