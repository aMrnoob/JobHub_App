package com.example.jobhub.dto

import com.google.gson.annotations.SerializedName

data class CompanyDTO(
    @SerializedName("companyId") var companyId: Int? = null,
    @SerializedName("companyName") var companyName: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("address") var address: String? = null,
    @SerializedName("logoUrl") var logoUrl: String? = null,
    @SerializedName("website") var website: String? = null,
    @SerializedName("userId") var userId: Int? = null
)
