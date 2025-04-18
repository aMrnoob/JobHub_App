package com.example.jobhub.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainDialogApplyBinding
import com.example.jobhub.dto.*
import com.example.jobhub.entity.enumm.ApplicationStatus
import com.example.jobhub.service.ApplicationService
import com.example.jobhub.service.ResumeService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ApplyJobActivity : AppCompatActivity() {

    private lateinit var binding: MainDialogApplyBinding
    private var resumeUri: Uri? = null
    private var currentJob: ItemJobDTO? = null
    private var currentUser: UserDTO? = null

    private val jobApplicationService by lazy {
        RetrofitClient.createRetrofit().create(ApplicationService::class.java)
    }

    private val resumeService by lazy {
        RetrofitClient.createRetrofit().create(ResumeService::class.java)
    }

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

        loadUserAndJob()
        setupUI()
        setupCurrentDate()
    }

    private fun setupCurrentDate() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Date()
        binding.tvCurrentDate.text = dateFormat.format(currentDate)
    }

    private fun loadUserAndJob() {
        val prefs = getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val userJson = prefs.getString("currentUser", null)
        val jobJson = prefs.getString("job", null)

        currentUser = Gson().fromJson(userJson, UserDTO::class.java)
        currentJob = Gson().fromJson(jobJson, ItemJobDTO::class.java)

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
        binding.ivBack.setOnClickListener { onBackPressed() }

        binding.btnChooseResume.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/pdf" }
            getResumeFile.launch(intent)
        }

        binding.btnSubmitApplication.setOnClickListener {
            if (validateInputs()) {
                uploadResumeAndSubmit()
            }
        }

        binding.tvCurrentStatus.text = "REVIEWED"
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
            val file = File(cacheDir, "resume.pdf")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }

            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            ApiHelper().callApi(
                context = this,
                call = jobApplicationService.uploadResume("Bearer $token", multipartBody),
                onSuccess = { resumeUrl ->
                    submitApplication(token, resumeUrl)
                },
                onError = {
                    showToast("Tải lên CV thất bại.")
                    binding.progressBar.visibility = View.GONE
                }
            )

        } ?: run {
            showToast("Vui lòng chọn CV")
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun submitApplication(token: String, resumeUrl: String?) {
        val jobId = currentJob?.jobId
        val userId = currentUser?.userId
        val coverLetter = binding.edtCoverLetter.text.toString().trim()

        if (jobId == null || userId == null || currentUser == null) {
            showToast("Thiếu thông tin ID.")
            binding.progressBar.visibility = View.GONE
            return
        }

        val applicationDTO = ApplicationDTO(
            applicationId = null,
            jobDTO = currentJob!!,
            userDTO = currentUser!!,
            coverLetter = coverLetter,
            status = ApplicationStatus.REVIEWED,
            applicationDate = Date(),
        )

        ApiHelper().callApi(
            context = this,
            call = jobApplicationService.applyForJob("Bearer $token", applicationDTO),
            onSuccess = { submittedApp ->
                // Now create the Resume entry after application is submitted
                createResumeEntry(token, submittedApp!!.applicationId, resumeUrl)
            },
            onError = {
                showToast("Nộp đơn thất bại.")
                binding.progressBar.visibility = View.GONE
            }
        )
    }

    private fun createResumeEntry(token: String, applicationId: Int?, resumeUrl: String?) {
        if (applicationId == null || resumeUrl == null) {
            showToast("Thiếu thông tin resume hoặc application ID.")
            binding.progressBar.visibility = View.GONE
            return
        }

        val resumeDTO = ResumeDTO(
            resumeId = null,
            applicationId = applicationId,
            resumeUrl = resumeUrl,
            createdAt = Date(),
            updatedAt = Date()
        )

        ApiHelper().callApi(
            context = this,
            call = resumeService.createResume("Bearer $token", resumeDTO),
            onSuccess = { _ ->
                showToast("Nộp đơn và CV thành công!")
                binding.progressBar.visibility = View.GONE
                finish()
            },
            onError = {
                showToast("Nộp đơn thành công nhưng lưu CV thất bại.")
                binding.progressBar.visibility = View.GONE
                finish()
            }
        )
    }

    private fun getAuthToken(): String? {
        return getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
            .getString("authToken", null)
            ?.takeIf { it.isNotBlank() }
            ?: run {
                showToast("Vui lòng đăng nhập.")
                null
            }
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
}