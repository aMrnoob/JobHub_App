package com.example.jobhub.service

import com.example.jobhub.dto.employer.CompanyInfo
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CompanyService {
    @POST("api/company/add-company")
    fun addCompany(@Body companyInfo: CompanyInfo): Call<ApiResponse<Void>>

    @POST("api/company/get-all-companies")
    fun getAllCompanies(@Header("token") token: String): Call<ApiResponse<List<CompanyInfo>>>
}