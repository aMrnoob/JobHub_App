package com.example.jobhub.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.ActivityApplicantBinding
import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.dto.NotificationDTO
import com.example.jobhub.dto.StatusApplicantDTO
import com.example.jobhub.entity.enumm.ApplicationStatus
import com.example.jobhub.service.ApplicationService
import com.example.jobhub.service.NotificationService
import com.example.jobhub.utils.ResumeViewerUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class ApplicantActivity : BaseActivity() {

    private lateinit var sharedPrefs: SharedPrefsManager
    private lateinit var binding: ActivityApplicantBinding
    private lateinit var statusApplicantDTO: StatusApplicantDTO

    private var applicationDTO: ApplicationDTO? = null

    private val applicationService: ApplicationService by lazy { RetrofitClient.createRetrofit().create(ApplicationService::class.java) }
    private val notificationService by lazy { RetrofitClient.createRetrofit().create(NotificationService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefs = SharedPrefsManager(this)
        applicationDTO = sharedPrefs.getCurrentApplication()

        showApplication()
        setUpSpinner()
        setUpInterviewDate()

        statusApplicantDTO = StatusApplicantDTO().apply {
            applicationId = applicationDTO?.applicationId ?: 0
            interviewDate = applicationDTO?.interviewDate
            status = applicationDTO?.status ?: ApplicationStatus.APPLIED
            message = applicationDTO?.coverLetter ?: ""
        }

        binding.btnSendMessage.setOnClickListener {
            val selectedStatusText = binding.tvSelectedStatus.text.toString()
            val status = mapStatusFromText(selectedStatusText)
            val message = binding.edtMessage.text.toString()
            statusApplicantDTO.applicationId = applicationDTO?.applicationId
            statusApplicantDTO.interviewDate = applicationDTO?.interviewDate

            if (status != null) {
                statusApplicantDTO.status = status
                statusApplicantDTO.message = message
            }

            updateStatusApplicant()
        }

        binding.btnReviewCV.setOnClickListener { applicationDTO?.let { it1 -> openResume(it1.resumeUrl) } }
    }

    private fun showApplication() {
        val base64Image = applicationDTO?.userDTO?.imageUrl
        if (!base64Image.isNullOrEmpty()) {
            try {
                val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                binding.userAvatar.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("ApplicationAdapter", "Lỗi decode base64: ${e.message}")
                binding.userAvatar.setImageResource(R.drawable.icon_upload_image)
            }
        } else {
            binding.userAvatar.setImageResource(R.drawable.icon_upload_image)
        }

        binding.tvFullName.text = applicationDTO?.userDTO?.fullName
        binding.tvEmail.text = applicationDTO?.userDTO?.email
        binding.tvPhone.text = applicationDTO?.userDTO?.phone
        binding.tvStatus.text = applicationDTO?.status.toString()
    }

    @SuppressLint("InflateParams")
    private fun setUpSpinner() {
        val statusList = when (applicationDTO?.status) {
            ApplicationStatus.INTERVIEW -> { listOf("Accept Application", "Reject Application") }
            ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED -> { listOf() }
            else -> { listOf("Schedule to Interview", "Accept Application", "Reject Application") }
        }

        if (statusList.isEmpty()) {
            binding.tvSelectedStatus.isEnabled = false
            binding.btnSendMessage.isEnabled = false
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_select_status, null)
        val listView = dialogView.findViewById<ListView>(R.id.listStatus)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusList)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedStatus = statusList[position]
            binding.tvSelectedStatus.text = selectedStatus
            binding.edtMessage.setText("")

            if(selectedStatus == "Accept Application" || selectedStatus == "Reject Application") {
                binding.edtDate.isEnabled = false
                binding.edtHour.isEnabled = false
                binding.edtDate.text = SpannableStringBuilder("20/5/2025")
                binding.edtHour.text = SpannableStringBuilder("12:00")
            } else {
                binding.edtDate.isEnabled = true
                binding.edtHour.isEnabled = true
            }
            dialog.dismiss()
        }

        binding.tvSelectedStatus.setOnClickListener {
            dialog.show()
        }
    }

    private fun setUpInterviewDate() {
        val status = applicationDTO?.status

        binding.edtDate.isEnabled = false
        binding.edtHour.isEnabled = false

        if (status == ApplicationStatus.APPLIED || status == null) {
            binding.edtDate.isEnabled = true
            binding.edtHour.isEnabled = true
            val updateInterviewDate = {
                val dateStr = binding.edtDate.text.toString()
                val timeStr = binding.edtHour.text.toString()
                if (dateStr.isNotEmpty() && timeStr.isNotEmpty()) {
                    val interviewDate = getInterviewDateTime(dateStr, timeStr)
                    if (interviewDate != null) {
                        applicationDTO?.interviewDate = interviewDate
                        statusApplicantDTO.interviewDate = interviewDate

                    }
                }
            }

            binding.edtDate.setOnClickListener {
                val calendar = Calendar.getInstance()
                val datePicker = DatePickerDialog(
                    this,
                    { _, year, month, dayOfMonth ->
                        val selectedDate = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
                        binding.edtDate.setText(selectedDate)
                        updateInterviewDate()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.show()
            }

            binding.edtHour.setOnClickListener {
                val calendar = Calendar.getInstance()
                val timePicker = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        val selectedTime = "%02d:%02d".format(hourOfDay, minute)
                        binding.edtHour.setText(selectedTime)
                        updateInterviewDate()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            }
        } else if ((status == ApplicationStatus.INTERVIEW || status == ApplicationStatus.ACCEPTED ||
                    status == ApplicationStatus.REJECTED) && applicationDTO?.interviewDate != null) {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            binding.edtMessage.isEnabled = false
            binding.edtDate.setText(applicationDTO?.interviewDate?.format(formatter))
            binding.edtHour.setText(applicationDTO?.interviewDate?.format(timeFormatter))
        }
    }

    private fun getInterviewDateTime(dateStr: String, timeStr: String): LocalDateTime? {
        return try {
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val datePart = dateFormatter.parse(dateStr)
            val timePart = timeFormatter.parse(timeStr)

            val localDate = java.time.LocalDate.from(datePart)
            val localTime = java.time.LocalTime.from(timePart)

            LocalDateTime.of(localDate, localTime)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun mapStatusFromText(statusText: String): ApplicationStatus? {
        return when (statusText) {
            "Schedule to Interview" -> ApplicationStatus.INTERVIEW
            "Accept Application" -> ApplicationStatus.ACCEPTED
            "Reject Application" -> ApplicationStatus.REJECTED
            else -> null
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateStatusApplicant() {
        val token = sharedPrefs.authToken ?: return

        ApiHelper().callApi(
            context = this,
            call = applicationService.updateStatusApplication("Bearer $token", statusApplicantDTO),
            onSuccess = {
                createNotificationForCandidate()
                finish()
            }
        )
    }

    private fun createNotificationForCandidate() {
        val appId = applicationDTO?.applicationId ?: return
        val companyId = applicationDTO?.jobDTO?.company?.companyId ?: return
        val userId = applicationDTO?.userDTO?.userId ?: return
        val receiverId = applicationDTO?.userDTO?.userId ?: return
        val status = statusApplicantDTO.status
        val message = binding.edtMessage.text.toString().trim()

        val notificationContent = when (status) {
            ApplicationStatus.INTERVIEW -> {
                val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                val dateStr = statusApplicantDTO.interviewDate?.format(dateFormatter) ?: "chưa xác định"
                "Bạn đã được mời phỏng vấn cho vị trí ${applicationDTO?.jobDTO?.title} vào lúc $dateStr."
            }
            ApplicationStatus.ACCEPTED -> "Chúc mừng! Đơn ứng tuyển cho vị trí ${applicationDTO?.jobDTO?.title} đã được chấp nhận."
            ApplicationStatus.REJECTED -> "Đơn ứng tuyển cho vị trí ${applicationDTO?.jobDTO?.title} đã bị từ chối."
            else -> "Có cập nhật mới cho đơn ứng tuyển vị trí ${applicationDTO?.jobDTO?.title}."
        }

        val finalContent = if (message.isNotEmpty()) {
            "$notificationContent\nGhi chú: $message"
        } else {
            notificationContent
        }

        val notificationDTO = NotificationDTO(
            senderId = userId,
            receiverId = receiverId,
            companyId = companyId,
            applicationId = appId,
            content = finalContent
        )

        ApiHelper().callApi(
            context = this,
            call = notificationService.createNotification(notificationDTO),
            onSuccess = { finish() }
        )
    }

    private fun openResume(url: String) {
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
                ResumeViewerUtils.downloadAndOpenResume(this@ApplicantActivity, fullUrl)
            } catch (e: Exception) {
                Toast.makeText(this@ApplicantActivity, "Không thể mở CV: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}