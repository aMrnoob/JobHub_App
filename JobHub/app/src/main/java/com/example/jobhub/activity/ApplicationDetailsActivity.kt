package com.example.jobhub.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.ActivityApplicationDetailsBinding
import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.entity.enumm.ApplicationStatus
import com.example.jobhub.service.ApplicationService
import com.example.jobhub.service.ResumeService
import com.example.jobhub.utils.ResumeViewerUtils
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class ApplicationDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityApplicationDetailsBinding
    private lateinit var sharedPrefs: SharedPrefsManager
    private var applicationId: Int = 0
    private var resumeUrl: String? = null

    private val applicationService by lazy { RetrofitClient.createRetrofit().create(ApplicationService::class.java) }
    private val resumeService by lazy { RetrofitClient.createRetrofit().create(ResumeService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefs = SharedPrefsManager(this)

        applicationId = intent.getIntExtra("applicationId", 0)

        if (applicationId == 0) {
            showToast("Can not find application information")
            finish()
            return
        }

        setupToolbar()
        setupDeleteButton()
        loadApplicationDetails()
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupDeleteButton() {
        binding.btnDeleteApplication.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa đơn ứng tuyển này không? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa") { _, _ ->
                deleteApplication()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteApplication() {
        val token = sharedPrefs.authToken
        if (token == null) {
            showToast("Phiên đăng nhập đã hết hạn")
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE

        ApiHelper().callApi(
            context = this,
            call = applicationService.deleteApplication("Bearer $token", applicationId),
            onSuccess = {
                binding.progressBar.visibility = View.GONE
                showToast("Đã xóa đơn ứng tuyển thành công")
                setResult(RESULT_OK)
                finish()
            },
            onError = { error ->
                binding.progressBar.visibility = View.GONE
                binding.contentLayout.visibility = View.VISIBLE
                showToast("Không thể xóa đơn ứng tuyển: ${error ?: "Lỗi không xác định"}")
            }
        )
    }

    private fun loadApplicationDetails() {
        val token = sharedPrefs.authToken
        if (token == null) {
            showToast("Session expired")
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE

        ApiHelper().callApi(
            context = this,
            call = applicationService.getApplicationById("Bearer $token", applicationId),
            onSuccess = { application ->
                binding.progressBar.visibility = View.GONE
                if (application != null) {
                    displayApplicationDetails(application)
                    fetchResumeURL(token, application)
                } else {
                    showToast("Can not found application")
                    finish()
                }
            },
            onError = { error ->
                binding.progressBar.visibility = View.GONE
                showToast("Can not load application detail: ${error ?: "Unknown error"}")
                finish()
            }
        )
    }

    @SuppressLint("SetTextI18n")
    private fun fetchResumeURL(token: String, application: ApplicationDTO) {
        val appId = application.applicationId ?: return

        binding.progressBarResume.visibility = View.VISIBLE
        binding.tvResumeUrl.text = "Loading CV information..."
        binding.btnViewResume.visibility = View.GONE

        ApiHelper().callApi(
            context = this,
            call = resumeService.getResumeByApplicationId("Bearer $token", appId),
            onSuccess = { resumeDTO ->
                binding.progressBarResume.visibility = View.GONE
                resumeUrl = resumeDTO?.resumeUrl

                if (!resumeUrl.isNullOrEmpty()) {
                    val fileName = resumeUrl!!.substringAfterLast('/')
                    binding.tvResumeUrl.text = fileName.ifEmpty { resumeUrl }
                    binding.btnViewResume.visibility = View.VISIBLE
                    binding.btnViewResume.setOnClickListener {
                        openResume(resumeUrl!!)
                    }
                } else {
                    binding.tvResumeUrl.text = "CV has not been uploaded yet"
                    binding.btnViewResume.visibility = View.GONE
                }
            },
            onError = {
                binding.progressBarResume.visibility = View.GONE
                binding.tvResumeUrl.text = "Can not load CV information"
                binding.btnViewResume.visibility = View.GONE
            }
        )
    }

    private fun openResume(url: String) {
        binding.progressBarResume.visibility = View.VISIBLE
        binding.btnViewResume.isEnabled = false

        val fullUrl = if (url.startsWith("./") || url.startsWith("../")) {
            val baseUrl = RetrofitClient.getBaseUrl()
            val relativePath = url.replaceFirst("./", "")
            "$baseUrl$relativePath"
        } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
            val baseUrl = RetrofitClient.getBaseUrl()
            "$baseUrl$url"
        } else {
            url
        }

        lifecycleScope.launch {
            try {
                ResumeViewerUtils.downloadAndOpenResume(this@ApplicationDetailsActivity, fullUrl)
            } catch (e: Exception) {
                showToast("Can not open CV: ${e.message}")
            } finally {
                binding.progressBarResume.visibility = View.GONE
                binding.btnViewResume.isEnabled = true
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayApplicationDetails(application: ApplicationDTO) {
        binding.contentLayout.visibility = View.VISIBLE

        val job = application.jobDTO
        val company = job.company

        binding.tvJobTitle.text = job.title
        binding.tvCompanyName.text = company.companyName
        binding.tvJobLocation.text = job.location
        binding.tvJobSalary.text = job.salary

        Glide.with(this)
            .load(company.logoUrl)
            .placeholder(R.drawable.ic_company_placeholder)
            .into(binding.ivCompanyLogo)

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val applicationDate = application.applicationDate.format(formatter) ?: "N/A"
        binding.tvApplicationDate.text = "Submited date: $applicationDate"

        if (application.coverLetter.isNotEmpty()) {
            binding.tvCoverLetter.text = application.coverLetter
            binding.cvCoverLetter.visibility = View.VISIBLE
        } else {
            binding.cvCoverLetter.visibility = View.GONE
        }

        val statusText = when (application.status) {
            ApplicationStatus.APPLIED -> "Submited"
            ApplicationStatus.ACCEPTED -> "Accepted"
            ApplicationStatus.REJECTED -> "Rejected"
            ApplicationStatus.INTERVIEW -> "Interview"
        }
        binding.tvStatus.text = statusText

        binding.tvStatus.setBackgroundResource(
            when (application.status) {
                ApplicationStatus.APPLIED -> R.drawable.bg_status_applied
                ApplicationStatus.ACCEPTED -> R.drawable.bg_status_accepted
                ApplicationStatus.REJECTED -> R.drawable.bg_status_rejected
                ApplicationStatus.INTERVIEW -> R.drawable.bg_status_interview
            }
        )

        binding.tvJobDescription.text = job.description
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}