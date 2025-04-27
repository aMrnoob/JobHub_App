package com.example.jobhub.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Outline
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.jobhub.anim.AnimationHelper
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
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
    private lateinit var sharedPrefs: SharedPrefsManager

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
                bindingProfile.uploadedImageView.apply {
                    clipToOutline = true
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View, outline: Outline) {
                            val size = view.width.coerceAtMost(view.height)
                            outline.setOval(0, 0, size, size)
                        }
                    }
                }
                bindingProfile.uploadedImageView.visibility = View.VISIBLE
                (bindingProfile.userAvatar.parent as View).visibility = View.GONE

            } else {
                Log.e("ImagePicker", "bindingProfile not working when created")
            }
        } else {
            Log.e("ImagePicker", "Can't select photo")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPrefs = SharedPrefsManager(this)

        bindingChooseProfile = ChooseProfileBinding.inflate(layoutInflater)
        setContentView(bindingChooseProfile.root)

        showStep(currentStep)
    }

    private fun showStep(step: Int) {
        when (step) {
            1 -> {
                bindingChooseProfile = ChooseProfileBinding.inflate(layoutInflater)
                setContentView(bindingChooseProfile.root)

                bindingChooseProfile.tvFindingJob.setOnClickListener {
                    AnimationHelper.animateScale(it)
                    role = Role.JOB_SEEKER
                    handleClick(it, Role.JOB_SEEKER)
                }

                bindingChooseProfile.tvFindingStaff.setOnClickListener {
                    AnimationHelper.animateScale(it)
                    role = Role.EMPLOYER
                    handleClick(it, Role.EMPLOYER)
                }
            }
            2 -> {
                bindingChooseJob = ChooseJobBinding.inflate(layoutInflater)
                setContentView(bindingChooseJob.root)

                bindingChooseJob.btnComeBack.setOnClickListener {
                    AnimationHelper.animateScale(it)
                    currentStep = 1
                    showStep(currentStep)
                }

                jobSelectionListeners()

                bindingChooseJob.btnNext.setOnClickListener {
                    AnimationHelper.animateScale(it)
                    currentStep = 3
                    showStep(currentStep)
                }
            }
            3 -> {
                bindingProfile = ActivityProfileBinding.inflate(layoutInflater)
                setContentView(bindingProfile.root)

                val token = sharedPrefs.authToken

                if (!token.isNullOrBlank()) {
                    getUserInfo(token)
                } else {
                    Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    return
                }

                bindingProfile.edtDateOfBirth.setOnClickListener {
                    AnimationHelper.animateScale(it)
                    showDatePicker()
                }

                bindingProfile.uploadImage.setOnClickListener {
                    AnimationHelper.animateScale(it)
                    openImagePicker()
                }

                bindingProfile.uploadedImageView.setOnClickListener {
                    AnimationHelper.animateScale(it)
                    openImagePicker()
                }

                bindingProfile.btnNext.setOnClickListener {
                    AnimationHelper.animateScale(it)
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
            onStart = {
                showLoading()
            },
            onComplete = {
                hideLoading()
            },
            onSuccess = {
                sharedPrefs.role = userDTO.role
                sharedPrefs.fullName = userDTO.fullName

                currentStep = 4
                showStep(currentStep)
            },
            onError = { errorMsg ->
                Toast.makeText(this, "Error updating profile: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun getUserInfo(token: String) {
        if (token.isBlank()) return

        ApiHelper().callApi(
            context = this,
            call = userService.getUser("Bearer $token"),
            onStart = {
                showLoading()
            },
            onComplete = {
                hideLoading()
            },
            onSuccess = { userDTO ->
                userDTO?.let {
                    runOnUiThread {
                        it.userId?.let { id -> userId = id }
                        bindingProfile.edtEmail.setText(it.email)

                        it.fullName?.let { name -> bindingProfile.edtFullName.setText(name) }
                        it.phone?.let { phone -> bindingProfile.edtPhone.setText(phone) }
                        it.address?.let { address -> bindingProfile.edtAddress.setText(address) }
                        it.dateOfBirth?.let { dob -> bindingProfile.edtDateOfBirth.setText(dob) }

                        if (it.role != null && it.role != Role.UNDEFINED) {
                            role = it.role
                        }
                    }
                }
            },
            onError = { errorMsg ->
                Toast.makeText(this, "Error fetching user info: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showLoading() {
        if (::bindingProfile.isInitialized) {
            bindingProfile.btnNext.isEnabled = false
            bindingProfile.btnNext.text = "Processing..."

            bindingProfile.loadingOverlay.visibility = View.VISIBLE
            bindingProfile.progressBar.visibility = View.VISIBLE
        }
    }

    private fun hideLoading() {
        if (::bindingProfile.isInitialized) {
            bindingProfile.btnNext.isEnabled = true
            bindingProfile.btnNext.text = "Update Profile"

            bindingProfile.loadingOverlay.visibility = View.GONE
            bindingProfile.progressBar.visibility = View.GONE
        }
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
        bindingChooseProfile.tvFindingJob.isSelected = false
        bindingChooseProfile.tvFindingStaff.isSelected = false
        view.isSelected = true
    }

    private fun selectRole() {
        currentStep = 2
        showStep(currentStep)
    }

    private fun jobSelectionListeners() {
        jobTitleMap.forEach { (_, textView) ->
            textView.setOnClickListener {
                AnimationHelper.animateScale(it)
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
        var isValid = true

        if (selectedImageUri == null) {
            isValid = false
            Toast.makeText(this, "Please upload your profile image.", Toast.LENGTH_SHORT).show()
        }

        if (bindingProfile.edtFullName.text.toString().isBlank()) {
            isValid = false
            Toast.makeText(this, "Please enter your full name.", Toast.LENGTH_SHORT).show()
        }

        if (bindingProfile.edtEmail.text.toString().isBlank()) {
            isValid = false
            Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show()
        }

        if (bindingProfile.edtDateOfBirth.text.toString().isBlank()) {
            isValid = false
            Toast.makeText(this, "Please select your date of birth.", Toast.LENGTH_SHORT).show()
        }

        if (bindingProfile.edtPhone.text.toString().isBlank()) {
            isValid = false
            Toast.makeText(this, "Please enter your phone number.", Toast.LENGTH_SHORT).show()
        }

        if (bindingProfile.edtAddress.text.toString().isBlank()) {
            isValid = false
            Toast.makeText(this, "Please enter your address.", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }

    private fun encodeImageToBase64(): String? {
        selectedImageUri?.let {
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