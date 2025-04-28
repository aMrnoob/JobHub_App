package com.example.jobhub.dto

import com.google.gson.annotations.SerializedName

data class ItemCompanyDTO(
    @SerializedName("companyId") val companyId: Int,
    @SerializedName("companyName") var companyName: String? = "",
    @SerializedName("logoUrl") val logoUrl: String? = "",
    @SerializedName("description") var description: String? = "",
)

