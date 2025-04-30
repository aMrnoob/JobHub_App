package com.example.jobhub.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.MainDialogApplyBinding
import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.dto.NotificationDTO
import com.example.jobhub.dto.ResumeDTO
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.entity.enumm.ApplicationStatus
import com.example.jobhub.service.ApplicationService
import com.example.jobhub.service.NotificationService
import com.example.jobhub.service.ResumeService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class ApplyJobActivity : BaseActivity() {

    private lateinit var binding: MainDialogApplyBinding
    private lateinit var sharedPrefs: SharedPrefsManager

    private var resumeUri: Uri? = null
    private var currentJob: ItemJobDTO? = null
    private var currentUser: UserDTO? = null

    private val jobApplicationService by lazy { RetrofitClient.createRetrofit().create(ApplicationService::class.java) }
    private val resumeService by lazy { RetrofitClient.createRetrofit().create(ResumeService::class.java) }
    private val notificationService by lazy { RetrofitClient.createRetrofit().create(NotificationService::class.java) }

    private val getResumeFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            resumeUri = result.data?.data
            resumeUri?.let {
                val fileName = getFileNameFromUri(it) ?: "resume.pdf"
                binding.tvResumeStatus.text = fileName
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainDialogApplyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefs = SharedPrefsManager(this)

        loadUserAndJob()
        setupUI()
        setupCurrentDate()
    }

    private fun setupCurrentDate() {
        val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val currentDate = LocalDateTime.now()
        val date = Date.from(currentDate.atZone(ZoneId.systemDefault()).toInstant())
        binding.tvCurrentDate.text = dateFormat.format(date)
    }

    private fun loadUserAndJob() {
        val userJson = sharedPrefs.getString("currentUser")
        currentUser = if(!userJson.isNullOrEmpty()) {
            Gson().fromJson(userJson, UserDTO::class.java)
        } else {
            null
        }

        currentJob = sharedPrefs.getCurrentJob()

        if (currentUser == null || currentJob == null) {
            showToast("Thiếu thông tin người dùng hoặc công việc.")
            finish()
        } else {
            displayJobInfo()
        }
    }

    private fun displayJobInfo() {
        currentJob?.let { job ->
            binding.tvJobTitle.text = job.title
            binding.tvCompanyName.text = job.company.companyName

            Glide.with(this)
                .load(job.company.logoUrl)
                .placeholder(R.drawable.error_image)
                .into(binding.ivCompanyLogo)
        }
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnChooseResume.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/pdf" }
            getResumeFile.launch(intent)
        }

        binding.btnSubmitApplication.setOnClickListener {
            if (validateInputs()) {
                checkExistingApplication()
            }
        }

        binding.tvCurrentStatus.text = "APPLIED"
    }

    private fun checkExistingApplication() {
        val token = getAuthToken() ?: return
        val jobId = currentJob?.jobId ?: return
        val userId = currentUser?.userId ?: return

        binding.progressBar.visibility = View.VISIBLE

        ApiHelper().callApi(
            context = this,
            call = jobApplicationService.getApplicationsByUserId("Bearer $token", userId),
            onSuccess = { applications ->
                if (applications != null) {
                    val alreadyApplied = applications.any { it.jobDTO.jobId == jobId }
                    if (alreadyApplied) {
                        showToast("You have already applied for this job")
                        binding.progressBar.visibility = View.GONE
                    } else {
                        uploadResumeAndSubmit()
                    }
                } else {
                    uploadResumeAndSubmit()
                }
            },
            onError = { error ->
                uploadResumeAndSubmit()
            }
        )
    }

    private fun validateInputs(): Boolean {
        val coverLetter = binding.edtCoverLetter.text.toString().trim()
        if (coverLetter.isEmpty()) {
            binding.edtCoverLetter.error = "Vui lòng nhập thư xin việc"
            return false
        }

        if (resumeUri == null) {
            showToast("Vui lòng chọn CV của bạn")
            return false
        }

        return true
    }

    private fun uploadResumeAndSubmit() {
        val token = getAuthToken() ?: return
        binding.progressBar.visibility = View.VISIBLE

        resumeUri?.let { uri ->
            try {
                val file = File(cacheDir, "resume.pdf")
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        val bytesCopied = input.copyTo(output)
                    }
                }

                val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)
                ApiHelper().callApi(
                    context = this,
                    call = jobApplicationService.uploadResume("Bearer $token", multipartBody),
                    onSuccess = { resumeUrl ->
                        if (resumeUrl != null) {
                            submitApplication(token, resumeUrl)
                        } else {
                            binding.progressBar.visibility = View.GONE
                        }
                    },
                    onError = { error ->
                        showToast("Tải lên CV thất bại: $error")
                        binding.progressBar.visibility = View.GONE
                    }
                )
            } catch (e: Exception) {
                showToast("Lỗi xử lý file: ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
        } ?: run {
            showToast("Vui lòng chọn CV")
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun submitApplication(token: String, resumeUrl: String) {
        val jobId = currentJob?.jobId
        val userId = currentUser?.userId
        val coverLetter = binding.edtCoverLetter.text.toString().trim()

        if (jobId == null || userId == null || currentUser == null || currentJob == null) {
            showToast("Thiếu thông tin ID.")
            binding.progressBar.visibility = View.GONE
            return
        }

        val applicationDTO = ApplicationDTO(
            applicationId = null,
            jobDTO = currentJob!!,
            userDTO = currentUser!!,
            coverLetter = coverLetter,
            status = ApplicationStatus.APPLIED,
            applicationDate = LocalDateTime.now(),
            resumeUrl = resumeUrl
        )

        ApiHelper().callApi(
            context = this,
            call = jobApplicationService.applyForJob("Bearer $token", applicationDTO),
            onSuccess = { submittedApp ->
                if (submittedApp != null && submittedApp.applicationId != null) {
                    createResumeEntry(token, submittedApp.applicationId, resumeUrl)
                } else {
                    showToast("Nộp đơn thành công nhưng thiếu ID ứng dụng.")
                    binding.progressBar.visibility = View.GONE
                    finish()
                }
            }
        )
    }

    private fun createResumeEntry(token: String, applicationId: Int, resumeUrl: String) {
        val resumeDTO = ResumeDTO(
            resumeId = 0,
            applicationId = applicationId,
            resumeUrl = resumeUrl,
            createdAt = LocalDateTime.now().withNano(0),
            updatedAt = LocalDateTime.now().withNano(0),
        )

        ApiHelper().callApi(
            context = this,
            call = resumeService.createResume("Bearer $token", resumeDTO),
            onSuccess = { _ ->
                showToast("Nộp đơn và CV thành công!")
                createNotification(applicationId)
            },
            onError = { error ->
                showToast("Nộp đơn thành công nhưng lưu CV thất bại: $error")
                finish()
            }
        )
    }

    private fun getAuthToken(): String? {
        val token = sharedPrefs.authToken

        if (token.isNullOrBlank()) {
            showToast("Vui lòng đăng nhập để tiếp tục.")
            return null
        }
        return token
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex("_display_name")
            if (nameIndex >= 0 && it.moveToFirst()) it.getString(nameIndex) else null
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun SharedPrefsManager.getString(key: String): String? {
        return when (key) {
            "currentUser" -> {
                val userId = this.userId
                val fullName = this.fullName
                val email = this.email
                val role = this.role

                if (userId != null && fullName != null && email != null && role != null) {
                    val userDTO = UserDTO(userId, fullName, email, "", role)
                    Gson().toJson(userDTO)
                } else null
            }
            else -> null
        }
    }

    private fun createNotification(applicationId: Int) {
        val notificationDTO = NotificationDTO(
            senderId = currentUser?.userId ?: 0,
            companyId = currentJob?.company?.companyId ?: 0,
            applicationId = applicationId,
            content = "${currentUser?.fullName} applied this job: ${currentJob?.title}"
        )

        ApiHelper().callApi(
            context = this,
            call = notificationService.createNotification(notificationDTO),
            onSuccess = {
                binding.progressBar.visibility = View.GONE
                finish()
            },
            onError = {
                binding.progressBar.visibility = View.GONE
                finish()
            }
        )
    }
}