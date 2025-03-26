package com.example.jobhub.activity

import android.os.Bundle
import android.util.Log
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityCompanyBinding
import com.example.jobhub.dto.admin.UserInfo
import com.example.jobhub.dto.employer.CompanyInfo
import com.example.jobhub.service.CompanyService
import com.example.jobhub.service.UserService

class CompanyActivity : BaseActivity() {

    private lateinit var binding: ActivityCompanyBinding
    private val companyService: CompanyService by lazy {
        RetrofitClient.createRetrofit().create(CompanyService::class.java)
    }
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }

    private var userInfo: UserInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCompanyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAuthToken()?.let { decryptedToken(it) } ?: Log.e("MainActivity", "Invalid or empty token")

        binding.btnAdd.setOnClickListener {
            val companyName = binding.edtCompanyName.text.toString()
            val address = binding.edtAddress.text.toString()
            val logoUrl = binding.edtLogoUrl.text.toString()
            val website = binding.edtWebsite.text.toString()
            val description = binding.edtDescription.text.toString()

            val companyInfo = CompanyInfo(null, companyName, userInfo, address, logoUrl, website, description)

            addCompany(companyInfo)
        }

        binding.btnComeBack.setOnClickListener {
            finish()
        }
    }

    private fun addCompany(companyInfo: CompanyInfo) {
        ApiHelper().callApi(
            context = this,
            call = companyService.addCompany(companyInfo),
            onSuccess = { }
        )
    }

    private fun getAuthToken(): String? {
        return getSharedPreferences("JobHubPrefs", MODE_PRIVATE)
            .getString("authToken", null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun decryptedToken(token: String) {
        ApiHelper().callApi(
            context = this,
            call = userService.getUserInfo("Bearer $token"),
            onSuccess = { userInfo = it }
        )
    }
}