package com.example.jobhub.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class Company(
    var companyId: Int? = null,
    var companyName: String? = null,
    var description: String? = null,
    var address: String? = null,
    var logoUrl: String? = null,
    var website: String? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var user: User? = null,
    var jobs: List<Job>? = emptyList()
) : Parcelable

