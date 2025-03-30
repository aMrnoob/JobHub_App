package com.example.jobhub.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityProfileBinding
import com.example.jobhub.databinding.ChooseJobBinding
import com.example.jobhub.databinding.ChooseProfileBinding
import com.example.jobhub.dto.admin.UserInfo
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.model.ApiResponse
import com.example.jobhub.service.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
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
    private var selectedImageUri: Uri? = null

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

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri

            if (::bindingProfile.isInitialized) {
                bindingProfile.uploadedImageView.setImageURI(uri)
                bindingProfile.uploadedImageView.invalidate()

                bindingProfile.iconUploadImage.visibility = View.GONE

                bindingProfile.uploadedImageView.visibility = View.VISIBLE
            } else {
                Log.e("ImagePicker", "bindingProfile not working when created")
            }

            validateFields()
        } else {
            Log.e("ImagePicker", "Can't select photo")
        }
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

                if (token != null && token.isNotBlank()) {
                    val cleanedToken = token.trim()
                    Log.d("Token", "'$token'")
                    decryptedToken(cleanedToken)
                } else {
                    Log.e("SelectProfileActivity", "Invalid or empty token")
                }

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
                    val fullName = bindingProfile.edtFullName.text.toString()
                    val email = bindingProfile.edtEmail.text.toString()
                    val dateString = bindingProfile.edtDateOfBirth.text.toString()
                    val phone = bindingProfile.edtPhone.text.toString()
                    val address = bindingProfile.edtAddress.text.toString()
                    val userInfo = UserInfo(userId = userId, fullName = fullName, role = role,
                        email = email, phone = phone, address = address, dateOfBirth = dateString
                    )

                    updateUser(userInfo)

                    currentStep = 4
                    showStep(currentStep)
                }
            }
            4 -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun updateUser(userInfo: UserInfo) {
        ApiHelper().callApi(
            context = this,
            call = userService.updateUser(userInfo),
            onSuccess = {
                currentStep = 4
                showStep(currentStep)
            }

            override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                Log.e("UpdateUser", "API thất bại: ${t.message}")
            }
        })
    }


    private fun decryptedToken(token: String) {
        val apiService = RetrofitClient.createRetrofit().create(UserService::class.java)
        apiService.getUserInfo("Bearer $token").enqueue(object : Callback<ApiResponse<UserInfo>> {
            override fun onResponse(call: Call<ApiResponse<UserInfo>>, response: Response<ApiResponse<UserInfo>>) {
                if (response.isSuccessful) {
                    Log.d("decryptedToken", "API call successful. Code: ${response.code()}")

                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.isSuccess) {
                        Log.d("decryptedToken", "User info: ${apiResponse.data}")

                        apiResponse.data?.let {
                            runOnUiThread {
                                it.userId?.let { id -> userId = id } ?: Log.e("decryptedToken", "User ID is null")
                                bindingProfile.edtEmail.setText(it.email)
                            }
                        } ?: run {
                            Log.e("decryptedToken", "User data is null in the response.")
                        }
                    } else {
                        Log.e("decryptedToken", "API response is not successful. Message: ${apiResponse?.message}")
                    }
                } else {
                    Log.e("decryptedToken", "API call failed. Code: ${response.code()}, Message: ${response.message()}")
                    Log.e("decryptedToken", "Error body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<UserInfo>>, t: Throwable) {
                Log.e("decryptedToken", "API call failed: ${t.message}", t)
            }
        })
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
            currentStep = 2
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

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(this, { _, year, month, day ->
            bindingProfile.edtDateOfBirth.setText("$day/${month + 1}/$year")
            validateFields()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePicker.show()
    }

    private fun validateFields(): Boolean {
        val fields = listOf(
            bindingProfile.edtFullName.text,
            bindingProfile.edtEmail.text,
            bindingProfile.edtPhone.text,
            bindingProfile.edtAddress.text,
            bindingProfile.edtDateOfBirth.text
        )

        val isValid = fields.all { it.isNotBlank() } && selectedImageUri != null
        bindingProfile.btnNext.isEnabled = isValid
        return isValid
    }

    private fun encodeImageToBase64(): String? {
        selectedImageUri?.let { uri ->
            val bitmap = (bindingProfile.uploadedImageView.drawable as? BitmapDrawable)?.bitmap
            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val byteArray = outputStream.toByteArray()
                return Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
        }
        return null
    }
}
