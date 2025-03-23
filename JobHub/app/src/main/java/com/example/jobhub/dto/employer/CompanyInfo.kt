package com.example.jobhub.dto.employer

import com.example.jobhub.dto.admin.UserInfo

data class CompanyInfo (
    val companyId: Int? = null,
    var companyName: String = "",
    val userInfo: UserInfo? = null,
    val address: String = "",
    val logoUrl: String = "",
    var website: String = "",
    val description: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
)