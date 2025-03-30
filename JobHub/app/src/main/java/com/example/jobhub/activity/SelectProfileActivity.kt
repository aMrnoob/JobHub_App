package com.example.jobhub.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityProfileBinding
import com.example.jobhub.databinding.ChooseJobBinding
import com.example.jobhub.databinding.ChooseProfileBinding
import com.example.jobhub.entity.User
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.service.UserService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.properties.Delegates

class SelectProfileActivity : BaseActivity() {

    private lateinit var bindingChooseProfile: ChooseProfileBinding
    private lateinit var bindingChooseJob: ChooseJobBinding
    private lateinit var bindingProfile: ActivityProfileBinding
    private lateinit var binding: ChooseProfileBinding
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }

    private var userId by Delegates.notNull<Int>()
    private lateinit var role: Role

    private var currentStep = 1
    private var lastClickTime: Long = 0

    private val jobTitleMap: Map<String, TextView> by lazy {
        mapOf(
            "Writer" to bindingChooseJob.tvWriter,
            "Art Design" to bindingChooseJob.tvArtDesign,
            "Human Resources" to bindingChooseJob.tvHR,
            "Programmer" to bindingChooseJob.tvProgramer,
            "Finance" to bindingChooseJob.tvFinance,
            "Customer Service" to bindingChooseJob.tvCustomerService,
            "Food Restaurant" to bindingChooseJob.tvFoodRestaurant,
            "Music Producer" to bindingChooseJob.tvMusicProducer
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ChooseProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvFindingJob.setOnClickListener {
            role = Role.JOB_SEEKER
            handleClick(it, Role.JOB_SEEKER)
        }

        binding.tvFindingStaff.setOnClickListener {
            role = Role.EMPLOYER
            handleClick(it, Role.EMPLOYER)
        }
    }

    private fun showStep(step: Int) {
        when (step) {
            1 -> {
                bindingChooseProfile = ChooseProfileBinding.inflate(layoutInflater)
                setContentView(bindingChooseProfile.root)

                bindingChooseProfile.tvFindingJob.setOnClickListener {
                    handleClick(it, Role.JOB_SEEKER)
                }

                bindingChooseProfile.tvFindingStaff.setOnClickListener {
                    handleClick(it, Role.EMPLOYER)
                }
            }
            2 -> {
                bindingChooseJob = ChooseJobBinding.inflate(layoutInflater)
                setContentView(bindingChooseJob.root)

                bindingChooseJob.btnComeBack.setOnClickListener {
                    currentStep = 1
                    showStep(currentStep)
                }

                jobSelectionListeners()

                bindingChooseJob.btnNext.setOnClickListener {
                    currentStep = 3
                    showStep(currentStep)
                }
            }
            3 -> {
                bindingProfile = ActivityProfileBinding.inflate(layoutInflater)
                setContentView(bindingProfile.root)

                val sharedPreferences = getSharedPreferences("JobHubPrefs", MODE_PRIVATE)
                val token = sharedPreferences.getString("authToken", null)

                if (!token.isNullOrBlank()) {
                    val cleaneken = token.trim()
                    Log.d("Token", "'$token'")
                    decrypteken(cleaneken)
                } else {
                    Log.e("SelectProfileActivity", "Invalid or empty token")
                }

                bindingProfile.edtDateOfBirth.setOnClickListener {
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                        val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                        val formattedDate = selectedDate.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                        bindingProfile.edtDateOfBirth.setText(formattedDate)
                    }, year, month, day)

                    datePicker.show()
                }

                bindingProfile.btnNext.setOnClickListener {
                    val fullName = bindingProfile.edtFullName.text.toString()
                    val email = bindingProfile.edtEmail.text.toString()
                    val dateString = bindingProfile.edtDateOfBirth.text.toString()
                    val phone = bindingProfile.edtPhone.text.toString()
                    val address = bindingProfile.edtAddress.text.toString()

                    try {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        val dateOfBirth = LocalDateTime.parse(dateString, formatter)

                        val user = User(
                            userId = userId,
                            fullName = fullName,
                            role = role,
                            email = email,
                            phone = phone,
                            address = address,
                            dateOfBirth = dateOfBirth
                        )

                        updateUser(user)

                        currentStep = 4
                        showStep(currentStep)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Invalid date format! Please use yyyy-MM-dd HH:mm:ss", Toast.LENGTH_SHORT).show()
                    }
                }

            }
            4 -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun updateUser(user: User) {
        ApiHelper().callApi(
            context = this,
            call = userService.updateUser(user),
            onSuccess = {
                currentStep = 4
                showStep(currentStep)
            }
        )
    }

    private fun decrypteken(token: String) {
        ApiHelper().callApi(
            context = this,
            call = userService.getUserInfo("Bearer $token"),
            onSuccess = { user ->
                user?.let {
                    it.userId.let { id ->
                        if (id != null) {
                            userId = id
                        }
                    }
                    bindingProfile.edtEmail.setText(it.email)
                }
            }
        )
    }

    private fun handleClick(view: View, selectedRole: Role) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastClickTime < 300) {
            role = selectedRole
            selectRole()
        } else {
            toggleSelection(view)
        }

        lastClickTime = currentTime
    }

    private fun toggleSelection(view: View) {
        binding.tvFindingJob.isSelected = false
        binding.tvFindingStaff.isSelected = false
        view.isSelected = true
    }

    private fun selectRole() {
        if (role == Role.EMPLOYER) {
            currentStep = 3
            showStep(currentStep)
        } else {
            currentStep = 2
            showStep(currentStep)
        }
    }

    private fun jobSelectionListeners() {
        jobTitleMap.forEach { (_, textView) ->
            textView.setOnClickListener {
                highlightSelectedJob(textView)
            }
        }
    }

    private fun highlightSelectedJob(selectedTextView: TextView) {
        jobTitleMap.values.forEach { it.isSelected = false }
        selectedTextView.isSelected = true
    }
}