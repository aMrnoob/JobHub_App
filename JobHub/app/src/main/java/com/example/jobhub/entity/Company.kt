package com.example.jobhub.entity

import java.time.LocalDateTime

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
    var jobs: Set<Job>? = emptySet()
)

