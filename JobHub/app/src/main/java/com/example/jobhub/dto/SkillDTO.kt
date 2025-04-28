package com.example.jobhub.dto

import com.google.gson.annotations.SerializedName

data class SkillDTO(
    @SerializedName("skillId") val skillId: Int,
    @SerializedName("skillName") var skillName: String
)

