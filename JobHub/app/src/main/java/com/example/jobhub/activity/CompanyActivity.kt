package com.example.jobhub.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityCompanyBinding
import com.example.jobhub.dto.admin.UserInfo
import com.example.jobhub.dto.employer.CompanyInfo
import com.example.jobhub.model.ApiResponse
import com.example.jobhub.service.CompanyService
import com.example.jobhub.service.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CompanyActivity : BaseActivity() {

    private lateinit var binding: ActivityCompanyBinding
    private val companyService: CompanyService by lazy {
        RetrofitClient.createRetrofit().create(CompanyService::class.java)
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
        companyService.addCompany(companyInfo).enqueue(object : Callback<ApiResponse<Void>> {
            override fun onResponse(
                call: Call<ApiResponse<Void>>,
                response: Response<ApiResponse<Void>>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@CompanyActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CompanyActivity, response.body()?.message ?: "Add company failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                Toast.makeText(this@CompanyActivity, "Error connection: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getAuthToken(): String? {
        return getSharedPreferences("JobHubPrefs", MODE_PRIVATE)
            .getString("authToken", null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun decryptedToken(token: String) {
        RetrofitClient.createRetrofit()
            .create(UserService::class.java)
            .getUserInfo("Bearer $token")
            .enqueue(object : Callback<ApiResponse<UserInfo>> {
                override fun onResponse(call: Call<ApiResponse<UserInfo>>, response: Response<ApiResponse<UserInfo>>) {
                    if (!response.isSuccessful) {
                        Log.e("decryptedToken", "API failed. Code: ${response.code()}, Message: ${response.message()}")
                        return
                    }

                    response.body()?.let { apiResponse ->
                        if (apiResponse.isSuccess) {
                            userInfo = apiResponse.data
                        } else {
                            Log.e("decryptedToken", "API unsuccessful: ${apiResponse.message}")
                        }
                    } ?: Log.e("decryptedToken", "Response body is null")
                }

                override fun onFailure(call: Call<ApiResponse<UserInfo>>, t: Throwable) {
                    Log.e("decryptedToken", "API call failed: ${t.message}", t)
                }
            })
    }
}