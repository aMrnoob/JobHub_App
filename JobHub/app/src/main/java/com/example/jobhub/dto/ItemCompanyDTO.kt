package com.example.jobhub.dto

import com.example.jobhub.entity.Company
import com.google.gson.annotations.SerializedName

data class ItemCompanyDTO(
    @SerializedName("companyId") val companyId: Int,
    @SerializedName("companyName") var companyName: String? = "",
    @SerializedName("logoUrl") val logoUrl: String? = "",
    @SerializedName("description") var description: String? = "",
)

fun ItemCompanyDTO.toCompany(): Company {
    return Company(
        companyId = this.companyId,
        companyName = this.companyName,
        description = this.description,
        logoUrl = this.logoUrl
    )
}