package com.example.jobhub.entity

import android.os.Parcelable
import com.example.jobhub.entity.enumm.ApplicationStatus
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class Application(
    val applicationId: Int,
    val job: Job,
    val user: User,
    val applicationDate: LocalDateTime?,
    val status: ApplicationStatus?,
    val coverLetter: String?,
    val resume: Resume?
) : Parcelable
