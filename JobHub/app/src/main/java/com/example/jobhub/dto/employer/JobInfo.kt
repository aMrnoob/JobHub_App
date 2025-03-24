package com.example.jobhub.dto.employer

import com.example.jobhub.dto.jobseeker.SkillInfo
import com.example.jobhub.entity.Application
import com.example.jobhub.entity.enumm.JobType

data class JobInfo(
    var jobId: Int? = null,
    var title: String = "",
    var companyInfo: CompanyInfo? = null,
    var description: String = "",
    var requirements: String = "",
    var salary: String = "",
    var location: String = "",
    var jobType: JobType? = null,
    var experienceRequired: String = "",
    var postingDate: String? = null,
    var expirationDate: String? = null,
    var requiredSkills: Set<SkillInfo> = emptySet(),
    var applications: Set<Application> = emptySet()
)
