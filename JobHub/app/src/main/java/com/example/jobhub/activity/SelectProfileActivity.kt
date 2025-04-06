package com.example.jobhub.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityProfileBinding
import com.example.jobhub.databinding.ChooseJobBinding
import com.example.jobhub.databinding.ChooseProfileBinding
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.service.UserService
import java.io.ByteArrayOutputStream
import java.util.Calendar


class SelectProfileActivity : BaseActivity() {

    private lateinit var bindingChooseProfile: ChooseProfileBinding
    private lateinit var bindingChooseJob: ChooseJobBinding
    private lateinit var bindingProfile: ActivityProfileBinding
    private lateinit var binding: ChooseProfileBinding
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }

    private var userId = -1
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

                if (!token.isNullOrBlank()) {
                    decryptedToken(token.trim())
                } else {
                    Log.e("SelectProfileActivity", "Invalid or empty token")
                }

                bindingProfile.edtDateOfBirth.setOnClickListener {
                    showDatePicker()
                }

                bindingProfile.uploadImage.setOnClickListener {
                    openImagePicker()
                }

                bindingProfile.uploadedImageView.setOnClickListener {
                    openImagePicker()
                }

                bindingProfile.btnNext.setOnClickListener {
                    if (validateFields()) {
                        val userInfo = UserDTO(
                            userId = userId,
                            fullName = bindingProfile.edtFullName.text.toString(),
                            role = role,
                            email = bindingProfile.edtEmail.text.toString(),
                            phone = bindingProfile.edtPhone.text.toString(),
                            imageUrl = encodeImageToBase64(),
                            address = bindingProfile.edtAddress.text.toString(),
                            dateOfBirth = bindingProfile.edtDateOfBirth.text.toString()
                        )

                        updateUser(userInfo)
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

    private fun updateUser(userDTO: UserDTO) {
        ApiHelper().callApi(
            context = this,
            call = userService.updateUser(userDTO),
            onSuccess = {
                currentStep = 4
                showStep(currentStep)
            }
        )
    }

    private fun decryptedToken(token: String) {
        if (token.isBlank()) return

        ApiHelper().callApi(
            context = this,
            call = userService.getUser("Bearer $token"),
            onSuccess = { userDTO ->
                userDTO?.let {
                    runOnUiThread {
                        it.userId?.let { id -> userId = id }
                        bindingProfile.edtEmail.setText(it.email)
                    }
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

    @SuppressLint("SetTextI18n")
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}