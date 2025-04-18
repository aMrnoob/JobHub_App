package com.example.jobhub.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Skill(
    var skillId: Int? = null,
    var skillName: String = "",
    var users: Set<User>? = emptySet(),
    var jobs: Set<Job> = emptySet()
) : Parcelable

