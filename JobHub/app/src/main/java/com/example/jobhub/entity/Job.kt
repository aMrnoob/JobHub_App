package com.example.jobhub.entity

import android.os.Parcelable
import com.example.jobhub.entity.enumm.JobType
import kotlinx.parcelize.Parcelize

@Parcelize
data class Job(
    var jobId: Int = 0,
    var title: String? = null,
    var description: String? = null,
    var requirements: String? = null,
    var salary: String? = null,
    var location: String? = null,
    var jobType: JobType? = null,
    var experienceRequired: String? = null,
    var postingDate: String? = null,
    var expirationDate: String? = null,
    var requiredSkills: Set<Skill>? = null,
    var company: Company? = null,
    var applications: Set<Application>? = null
) : Parcelable
