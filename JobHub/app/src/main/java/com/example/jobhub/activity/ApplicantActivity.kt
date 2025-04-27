package com.example.jobhub.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.ActivityApplicantBinding
import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.dto.StatusApplicantDTO
import com.example.jobhub.entity.enumm.ApplicationStatus
import com.example.jobhub.service.ApplicationService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class ApplicantActivity : BaseActivity() {

    private lateinit var sharedPrefs: SharedPrefsManager
    private lateinit var binding: ActivityApplicantBinding
    private lateinit var statusApplicantDTO: StatusApplicantDTO

    private var applicationDTO: ApplicationDTO? = null

    private val applicationService: ApplicationService by lazy { RetrofitClient.createRetrofit().create(ApplicationService::class.java) }

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
            val selectedStatusText = binding.actvStatus.text.toString()
            val status = mapStatusFromText(selectedStatusText)
            val message = binding.edtMessage.text.toString()
            Log.e("statusav", status.toString())
            statusApplicantDTO.applicationId = applicationDTO?.applicationId
            statusApplicantDTO.interviewDate = applicationDTO?.interviewDate

            if (status != null) {
                statusApplicantDTO.status = status
                statusApplicantDTO.message += " ^^ $message"
            }

            updateStatusApplicant()
        }
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
        binding.edtMessage.text = Editable.Factory.getInstance().newEditable(applicationDTO?.coverLetter?.substringAfterLast("^^")?.trim())
    }

    private fun setUpSpinner() {
        val statusList = listOf("Schedule to Interview", "Accept Application", "Reject Application")

        val adapter = ArrayAdapter(this, R.layout.item_dropdown_status, statusList)
        binding.actvStatus.setAdapter(adapter)

        binding.actvStatus.setOnItemClickListener { _, _, position, _ ->
            val selectedStatus = statusList[position]
            binding.actvStatus.setText(selectedStatus, false)
        }
    }

    private fun setUpInterviewDate() {
        val status = applicationDTO?.status

        if (status != ApplicationStatus.APPLIED) {
            binding.edtDate.isEnabled = true
            binding.edtHour.isEnabled = false
            return
        }

        val updateInterviewDate = {
            val dateStr = binding.edtDate.text.toString()
            val timeStr = binding.edtHour.text.toString()
            if (dateStr.isNotEmpty() && timeStr.isNotEmpty()) {
                val interviewDate = getInterviewDateTime(dateStr, timeStr)
                if (interviewDate != null) {
                    applicationDTO?.interviewDate = interviewDate
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
            onSuccess = { }
        )
    }
}