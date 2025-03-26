package com.example.jobhub.dto.employer

import com.example.jobhub.dto.admin.UserInfo

data class CompanyInfo (
    var companyId: Int? = null,
    var companyName: String = "",
    var userInfo: UserInfo? = null,
    var address: String = "",
    var logoUrl: String = "",
    var website: String = "",
    var description: String = "",
    var createdAt: String = "",
    var updatedAt: String = "",
)