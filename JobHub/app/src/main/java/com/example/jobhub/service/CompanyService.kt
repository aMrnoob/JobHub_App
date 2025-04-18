package com.example.jobhub.service

import com.example.jobhub.dto.CompanyDTO
import com.example.jobhub.entity.Company
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CompanyService {
    @POST("api/company/add-company")
    fun addCompany(@Body companyDTO: CompanyDTO): Call<ApiResponse<Void>>

    @POST("api/company/update-company")
    fun updateCompany(@Body companyDTO: CompanyDTO): Call<ApiResponse<Void>>

    @POST("api/company/delete-company")
    fun deleteCompany(@Body companyId: Int): Call<ApiResponse<Void>>

    @POST("api/company/get-all-companies-by-userId")
    fun getAllCompaniesByUserId(@Header("token") token: String): Call<ApiResponse<List<Company>>>
}