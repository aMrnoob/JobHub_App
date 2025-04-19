package com.example.jobhub.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import com.example.jobhub.anim.AnimationHelper
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.ActivityCompanyBinding
import com.example.jobhub.dto.CompanyDTO
import com.example.jobhub.entity.Company
import com.example.jobhub.service.CompanyService
import com.example.jobhub.validation.ValidationResult
import com.example.jobhub.validation.validateCompanyName

class CompanyActivity : BaseActivity() {

    private lateinit var binding: ActivityCompanyBinding
    private lateinit var sharedPrefs: SharedPrefsManager

    private val companyService: CompanyService by lazy { RetrofitClient.createRetrofit().create(CompanyService::class.java) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefs = SharedPrefsManager(this)

        val company = intent.getParcelableExtra<Company>("company")

        if (company != null) {
            binding.edtCompanyName.setText(company.companyName)
            binding.edtAddress.setText(company.address)
            binding.edtLogoUrl.setText(company.logoUrl)
            binding.edtWebsite.setText(company.website)
            binding.edtDescription.setText(company.description)
            binding.btnAdd.text = "Update"
            binding.tvCompany.text = "Edit company"
        }

        binding.btnAdd.setOnClickListener {
            val companyName = binding.edtCompanyName.text.toString()
            val address = binding.edtAddress.text.toString()
            val logoUrl = binding.edtLogoUrl.text.toString()
            val website = binding.edtWebsite.text.toString()
            val description = binding.edtDescription.text.toString()

            if (!validateField(validateCompanyName(companyName))) return@setOnClickListener
            if (!isValidInput(address, description, logoUrl, website)) {
                Toast.makeText(this, "Please fill in the information completely!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val companyDTO = CompanyDTO(
                company?.companyId,
                companyName,
                description,
                address,
                logoUrl,
                website,
                sharedPrefs.userId,
            )

            AnimationHelper.animateScale(it)
            if (company == null) {
                addCompany(companyDTO)
            } else {
                updateCompany(companyDTO)
            }
        }

        binding.btnComeBack.setOnClickListener {
            AnimationHelper.animateScale(it)
            finish()
        }
    }

    private fun addCompany(companyDTO: CompanyDTO) {
        ApiHelper().callApi(
            context = this,
            call = companyService.addCompany(companyDTO),
            onSuccess = { finish() }
        )
    }

    private fun updateCompany(companyDTO: CompanyDTO) {
        ApiHelper().callApi(
            context = this,
            call = companyService.updateCompany(companyDTO),
            onSuccess = { finish() }
        )
    }

    private fun isValidInput(vararg fields: String): Boolean { return fields.all { it.isNotBlank() } }

    private fun validateField(validationResult: ValidationResult): Boolean {
        return when (validationResult) {
            is ValidationResult.Error -> {
                Toast.makeText(this@CompanyActivity, validationResult.message, Toast.LENGTH_SHORT).show()
                false
            }
            is ValidationResult.Success -> true
        }
    }
}