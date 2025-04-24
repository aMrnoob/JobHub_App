package com.example.jobhub.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class ApplicationDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationDetailsBinding
    private lateinit var sharedPrefs: SharedPrefsManager
    private var applicationId: Int = 0
    private var resumeUrl: String? = null

    private val applicationService by lazy {
        RetrofitClient.createRetrofit().create(ApplicationService::class.java)
    }

    private val resumeService by lazy {
        RetrofitClient.createRetrofit().create(ResumeService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefs = SharedPrefsManager(this)

        applicationId = intent.getIntExtra("applicationId", 0)

        if (applicationId == 0) {
            showToast("Không thể tìm thấy thông tin ứng tuyển")
            finish()
            return
        }

        setupToolbar()
        loadApplicationDetails()
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadApplicationDetails() {
        val token = sharedPrefs.authToken
        if (token == null) {
            showToast("Phiên đăng nhập hết hạn")
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
                    showToast("Không tìm thấy thông tin ứng tuyển")
                    finish()
                }
            },
            onError = { error ->
                binding.progressBar.visibility = View.GONE
                showToast("Không thể tải thông tin ứng tuyển: ${error ?: "Lỗi không xác định"}")
                finish()
            }
        )
    }

    private fun fetchResumeURL(token: String, application: ApplicationDTO) {
        val appId = application.applicationId ?: return

        binding.progressBarResume.visibility = View.VISIBLE
        binding.tvResumeUrl.text = "Đang tải thông tin CV..."
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
                    binding.tvResumeUrl.text = "Chưa tải lên CV"
                    binding.btnViewResume.visibility = View.GONE
                }
            },
            onError = { error ->
                binding.progressBarResume.visibility = View.GONE
                binding.tvResumeUrl.text = "Không thể tải thông tin CV"
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
                showToast("Không thể mở CV: ${e.message}")
            } finally {
                binding.progressBarResume.visibility = View.GONE
                binding.btnViewResume.isEnabled = true
            }
        }
    }

    private fun displayApplicationDetails(application: ApplicationDTO) {
        binding.contentLayout.visibility = View.VISIBLE

        val job = application.jobDTO
        val company = job?.company

        binding.tvJobTitle.text = job?.title ?: "Không có tiêu đề"
        binding.tvCompanyName.text = company?.companyName ?: "Công ty không xác định"
        binding.tvJobLocation.text = job?.location ?: "Không có địa chỉ"
        binding.tvJobSalary.text = job?.salary ?: "Thương lượng"

        Glide.with(this)
            .load(company?.logoUrl)
            .placeholder(R.drawable.ic_company_placeholder)
            .into(binding.ivCompanyLogo)

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val applicationDate = application.applicationDate?.format(formatter) ?: "N/A"
        binding.tvApplicationDate.text = "Ngày nộp: $applicationDate"

        if (!application.coverLetter.isNullOrEmpty()) {
            binding.tvCoverLetter.text = application.coverLetter
            binding.cvCoverLetter.visibility = View.VISIBLE
        } else {
            binding.cvCoverLetter.visibility = View.GONE
        }

        val statusText = when (application.status) {
            ApplicationStatus.APPLIED -> "Đã nộp"
            ApplicationStatus.ACCEPTED -> "Đã chấp nhận"
            ApplicationStatus.REJECTED -> "Đã từ chối"
            ApplicationStatus.INTERVIEW -> "Phỏng vấn"
            else -> "Đã nộp"
        }
        binding.tvStatus.text = statusText

        binding.tvStatus.setBackgroundResource(
            when (application.status) {
                ApplicationStatus.APPLIED -> R.drawable.bg_status_applied
                ApplicationStatus.ACCEPTED -> R.drawable.bg_status_accepted
                ApplicationStatus.REJECTED -> R.drawable.bg_status_rejected
                ApplicationStatus.INTERVIEW -> R.drawable.bg_status_interview
                else -> R.drawable.bg_status_applied
            }
        )

        binding.tvJobDescription.text = job?.description ?: "Không có mô tả công việc"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}