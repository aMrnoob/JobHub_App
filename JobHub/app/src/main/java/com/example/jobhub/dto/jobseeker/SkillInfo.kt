package com.example.jobhub.dto.jobseeker

import com.example.jobhub.dto.admin.UserInfo
import com.example.jobhub.dto.employer.JobInfo

data class SkillInfo(
    var skillId: Int,
    var skillName: String,
    var users: Set<UserInfo>? = emptySet(),
    var jobs: Set<JobInfo> = emptySet()
)
