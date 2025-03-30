package com.example.jobhub.entity

import com.example.jobhub.dto.SkillDTO

data class Skill(
    var skillId: Int? = null,
    var skillName: String = "",
    var users: Set<User>? = emptySet(),
    var jobs: Set<Job> = emptySet()
)

fun Skill.toSkillDTO(): SkillDTO {
    return SkillDTO(
        skillId = this.skillId ?: 0,
        skillName = this.skillName
    )
}
