package com.example.jobhub.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityCompanyBinding
import com.example.jobhub.dto.CompanyDTO
import com.example.jobhub.entity.User
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

    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCompanyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAuthToken()?.let { decryptoken(it) } ?: Log.e("MainActivity", "Invalid or empty token")

        binding.btnAdd.setOnClickListener {
            val companyName = binding.edtCompanyName.text.toString()
            val address = binding.edtAddress.text.toString()
            val logoUrl = binding.edtLogoUrl.text.toString()
            val website = binding.edtWebsite.text.toString()
            val description = binding.edtDescription.text.toString()

            val companyDTO = CompanyDTO(null, companyName, description, address, logoUrl, website, user?.userId)

            addCompany(companyDTO)
        }

        binding.btnComeBack.setOnClickListener {
            finish()
        }
    }

    private fun addCompany(companyDTO: CompanyDTO) {
        ApiHelper().callApi(
            context = this,
            call = companyService.addCompany(companyDTO),
            onSuccess = { }
        )
    }

    private fun getAuthToken(): String? {
        return getSharedPreferences("JobHubPrefs", MODE_PRIVATE)
            .getString("authToken", null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun decryptoken(token: String) {
        ApiHelper().callApi(
            context = this,
            call = userService.getUserInfo("Bearer $token"),
            onSuccess = {
                user = it
                if (it != null) {
                    Log.d("UserCheck", "User loaded: ${it.userId}, ${it.email}")
                }
                if (it != null) {
                    if (it.userId == null) {
                        Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}