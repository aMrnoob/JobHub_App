package com.example.jobhub.dto

import com.example.jobhub.entity.enumm.JobType
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class ItemJobDTO(
    @SerializedName("jobId") var jobId: Int,
    @SerializedName("title") var title: String,
    @SerializedName("description") var description: String,
    @SerializedName("requirements") var requirements: String,
    @SerializedName("salary") var salary: String,
    @SerializedName("location") var location: String,
    @SerializedName("jobType") val jobType: JobType,
    @SerializedName("experienceRequired") val experienceRequired: String,
    @SerializedName("expirationDate")
    var expirationDate: LocalDateTime,
    @SerializedName("company")
    var company: ItemCompanyDTO,
    @SerializedName("requiredSkills")
    var requiredSkills: Set<SkillDTO>,

    @Transient var expirationDateStr: String? = null
)
