package com.example.jobhub

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ChooseJobBinding
import com.example.jobhub.databinding.ChooseProfileBinding
import com.example.jobhub.databinding.ProfileBinding
import com.example.jobhub.entity.User
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.model.ApiResponse
import com.example.jobhub.service.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar


class SelectProfileActivity : BaseActivity() {

    private lateinit var bindingChooseProfile: ChooseProfileBinding
    private lateinit var bindingChooseJob: ChooseJobBinding
    private lateinit var bindingProfile: ProfileBinding
    private lateinit var binding: ChooseProfileBinding

    private var user: User = User()

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

        val sharedPreferences = getSharedPreferences("JobHubPrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("authToken", null)

        if (token != null && token.isNotBlank()) {
            val cleanedToken = token.trim()
            Log.d("Token", "'$token'")
            decryptedToken(cleanedToken)
        } else {
            Log.e("SelectProfileActivity", "Invalid or empty token")
        }

        binding = ChooseProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvFindingJob.setOnClickListener {
            handleClick(it, Role.JOB_SEEKER)
        }

        binding.tvFindingStaff.setOnClickListener {
            handleClick(it, Role.EMPLOYER)
        }
    }

    private fun showStep(step: Int) {
        when (step) {
            1 -> {
                bindingChooseProfile = ChooseProfileBinding.inflate(layoutInflater)
                setContentView(bindingChooseProfile.root)

                bindingChooseProfile.tvFindingJob.setOnClickListener {
                    handleClick(it, Role.EMPLOYER)
                }

                bindingChooseProfile.tvFindingStaff.setOnClickListener {
                    handleClick(it, Role.JOB_SEEKER)
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
                bindingProfile = ProfileBinding.inflate(layoutInflater)
                setContentView(bindingProfile.root)

                bindingProfile.edtEmail.setText(user.email)

                bindingProfile.edtDateOfBirth.setOnClickListener {
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                        val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                        bindingProfile.edtDateOfBirth.setText(date)
                    }, year, month, day)

                    datePicker.show()
                }

                bindingProfile.btnNext.setOnClickListener {
                    user.fullName = bindingProfile.edtFullName.text.toString()
                    val dateString = bindingProfile.edtDateOfBirth.text.toString()
                    if (dateString.isNotEmpty()) {
                        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
                        val localDate = LocalDate.parse(dateString, formatter)
                        user.dateOfBirth = localDate.atStartOfDay()
                    }
                    user.phone = bindingProfile.edtPhone.text.toString()
                    user.address = bindingProfile.edtAddress.text.toString()

                    updateUser()

                    currentStep = 4
                    showStep(currentStep)
                }
            }
            4 -> {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun updateUser() {
        val apiService = RetrofitClient.createRetrofit().create(UserService::class.java)
        apiService.updateUser(user).enqueue(object : Callback<ApiResponse<Void>> {
            override fun onResponse(call: Call<ApiResponse<Void>>, response: Response<ApiResponse<Void>>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()?.let {
                        if (response.isSuccessful) {
                            currentStep = 4
                            showStep(currentStep)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {

            }
        })
    }

    private fun decryptedToken(token: String) {
        val apiService = RetrofitClient.createRetrofit().create(UserService::class.java)
        apiService.getUserInfo(token).enqueue(object : Callback<ApiResponse<User>> {
            override fun onResponse(call: Call<ApiResponse<User>>, response: Response<ApiResponse<User>>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()?.data?.let {
                        user = it
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {

            }
        })
    }

    private fun handleClick(view: View, selectedRole: Role) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastClickTime < 300) {
            user.role = selectedRole
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
        if (user.role == Role.EMPLOYER) {
            currentStep = 2
            showStep(currentStep)
        } else {
            currentStep = 3
            showStep(currentStep)
        }
    }

    private fun jobSelectionListeners() {
        jobTitleMap.forEach { (_, textView) ->
            textView.setOnClickListener {
                val jobTitle = textView.text.toString()

                highlightSelectedJob(textView)
            }
        }
    }

    private fun highlightSelectedJob(selectedTextView: TextView) {
        jobTitleMap.values.forEach { it.isSelected = false }
        selectedTextView.isSelected = true
    }
}