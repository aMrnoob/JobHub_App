package com.example.jobhub.dto

import com.example.jobhub.entity.Skill
import com.google.gson.annotations.SerializedName

data class SkillDTO(
    @SerializedName("skillId") val skillId: Int,
    @SerializedName("skillName") var skillName: String
)

fun SkillDTO.toSkill(): Skill {
    return Skill(
        skillId = this.skillId,
        skillName = this.skillName
    )
}
