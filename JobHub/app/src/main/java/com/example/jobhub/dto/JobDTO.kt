package com.example.jobhub.dto

import com.example.jobhub.entity.enumm.JobType

data class JobDTO(
    var jobId: Int? = null,
    var title: String? = null,
    var companyName: String? = null,
    var description: String? = null,
    var requirements: String? = null,
    var salary: String? = null,
    var location: String? = null,
    var jobType: JobType? = null,
    var experienceRequired: String? = null,
    var postingDate: String? = null,
    var expirationDate: String? = null,
    var requiredSkills: Set<SkillDTO> = emptySet(),
)
