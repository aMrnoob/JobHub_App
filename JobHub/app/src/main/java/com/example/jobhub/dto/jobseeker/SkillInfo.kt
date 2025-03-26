package com.example.jobhub.dto.jobseeker

import com.example.jobhub.dto.admin.UserInfo

data class SkillInfo(
    var skillId: Int? = null,
    var skillName: String,
    var users: Set<UserInfo>? = emptySet(),
)
