package com.example.jobhub.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.ApiService
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.TokenRequest
import com.example.jobhub.databinding.CreateAccountBinding
import com.example.jobhub.databinding.VerifyAccountBinding
import com.example.jobhub.dto.auth.OtpRequest
import com.example.jobhub.dto.auth.OtpVerifyRequest
import com.example.jobhub.dto.auth.Register_ResetPwdRequest
import com.example.jobhub.service.UserService
import com.example.jobhub.validation.ValidationResult
import com.example.jobhub.validation.validateEmail
import com.example.jobhub.validation.validatePassword
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class SignUpActivity : BaseActivity() {

    private lateinit var bindingRegister: CreateAccountBinding
    private lateinit var bindingVerify: VerifyAccountBinding
    private var countDownTimer: CountDownTimer? = null

    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }
    private val apiService: ApiService by lazy {
        RetrofitClient.createRetrofit().create(ApiService::class.java)
    }

    private var currentStep = 1
    private lateinit var otpCode: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var confirmPwd: String
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showStep(1)
    }

    private fun showStep(step: Int) {
        when (step) {
            1 -> {
                bindingRegister = CreateAccountBinding.inflate(layoutInflater)
                setContentView(bindingRegister.root)

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("856354548077-e00ibmh0ojbv416s43qldd8ec0j4o43m.apps.googleusercontent.com") // Thay bằng Web Client ID
                    .requestEmail()
                    .build()
                googleSignInClient = GoogleSignIn.getClient(this, gso)

                bindingRegister.invisiblePwd.setOnClickListener {
                    togglePasswordVisibility(true)
                }

                bindingRegister.invisibleConfirmPwd.setOnClickListener {
                    togglePasswordVisibility(false)
                }

                bindingRegister.btnSignUp.setOnClickListener {
                    email = bindingRegister.edtEmail.text.toString()
                    password = bindingRegister.edtPassword.text.toString()
                    confirmPwd = bindingRegister.edtConfirmPwd.text.toString()

                    if (!validateField(validateEmail(email))) return@setOnClickListener
                    if (!validateField(validatePassword(password))) return@setOnClickListener
                    if (password != confirmPwd) {
                        Toast.makeText(this@SignUpActivity, "Confirm password does not match.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    } else {
                        requestOtp(OtpRequest(email))
                    }
                }

                bindingRegister.btnHaveAccount.setOnClickListener {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }

                bindingRegister.btnGoogle.setOnClickListener {
                    val signInIntent = googleSignInClient.signInIntent
                    signInLauncher.launch(signInIntent)
                }

                bindingRegister.btnFacebook.setOnClickListener {

                }
            }
            2 -> {
                bindingVerify = VerifyAccountBinding.inflate(layoutInflater)
                setContentView(bindingVerify.root)

                val otpFields = listOf(
                    bindingVerify.otp1,
                    bindingVerify.otp2,
                    bindingVerify.otp3,
                    bindingVerify.otp4,
                    bindingVerify.otp5,
                    bindingVerify.otp6
                )

                setupOtpFields(otpFields)

                bindingVerify.btnComeBack.setOnClickListener {
                    startActivity(Intent(this, LoginActivity::class.java))
                }

                bindingVerify.btnSendCode.isEnabled = false

                bindingVerify.btnSendCode.setOnClickListener {
                    requestOtp(OtpRequest(email))
                    bindingVerify.btnSendCode.isEnabled = false
                }

                bindingVerify.btnConfirm.setOnClickListener {
                    otpCode = otpFields.joinToString("") { it.text.toString() }.trim()

                    if (otpCode.isEmpty()) {
                        Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
                    } else {
                        verifyOtp(OtpVerifyRequest(email, otpCode))
                    }
                }
            }
        }
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                Log.d("GoogleSignIn", "ID Token: $idToken")
                sendTokenToBackend(idToken)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Login Failed: ${e.statusCode}")
            }
        }
    }

    private fun sendTokenToBackend(idToken: String?) {
        if (idToken.isNullOrBlank()) return
        val request = TokenRequest(idToken)

        ApiHelper().callApi(
            context = this,
            call = apiService.sendGoogleToken(request),
            onSuccess = { apiResponse ->
                apiResponse?.let {
                    saveToken(it.token)
                    startActivity(Intent(this, SelectProfileActivity::class.java))
                    finish()
                }
            }
        )
    }

    private fun signUp(email: String, password: String) {
        ApiHelper().callApi(
            context = this,
            call = userService.register(Register_ResetPwdRequest(email, password)),
            onSuccess = { startActivity(Intent(this@SignUpActivity, LoginActivity::class.java)) }
        )
    }

    private fun requestOtp(otpRequest: OtpRequest) {
        ApiHelper().callApi(
            context = this,
            call = userService.otpRegister(otpRequest),
            onStart = { bindingRegister.progressBar.visibility = View.VISIBLE },
            onComplete = { bindingRegister.progressBar.visibility = View.VISIBLE },
            onSuccess = {
                currentStep = 2
                showStep(currentStep)
                startResendTimer()
            }
        )
    }

    private fun setupOtpFields(otpFields: List<EditText>) {
        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    } else if (s?.isEmpty() == true && i > 0) {
                        otpFields[i - 1].requestFocus()
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            otpFields[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.action == android.view.KeyEvent.ACTION_DOWN && otpFields[i].text.isEmpty() && i > 0) {
                    otpFields[i - 1].requestFocus()
                }
                false
            }
        }
    }

    private fun togglePasswordVisibility(isPasswordField: Boolean) {
        if (isPasswordField) {
            if (isPasswordVisible) {
                bindingRegister.edtPassword.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                bindingRegister.invisiblePwd.setImageResource(R.drawable.invisibility_icon)
            } else {
                bindingRegister.edtPassword.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                bindingRegister.invisiblePwd.setImageResource(R.drawable.visibility_icon)
            }
            bindingRegister.edtPassword.setSelection(bindingRegister.edtPassword.text.length)
            bindingRegister.edtPassword.transformationMethod = bindingRegister.edtPassword.transformationMethod
            isPasswordVisible = !isPasswordVisible
        } else {
            if (isConfirmPasswordVisible) {
                bindingRegister.edtConfirmPwd.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                bindingRegister.invisibleConfirmPwd.setImageResource(R.drawable.invisibility_icon)
            } else {
                bindingRegister.edtConfirmPwd.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                bindingRegister.invisibleConfirmPwd.setImageResource(R.drawable.visibility_icon)
            }
            bindingRegister.edtConfirmPwd.setSelection(bindingRegister.edtConfirmPwd.text.length)
            bindingRegister.edtConfirmPwd.transformationMethod = bindingRegister.edtConfirmPwd.transformationMethod
            isConfirmPasswordVisible = !isConfirmPasswordVisible
        }
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("JobHubPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("authToken", token).apply()
    }

    private fun validateField(validationResult: ValidationResult): Boolean {
        return when (validationResult) {
            is ValidationResult.Error -> {
                Toast.makeText(this@SignUpActivity, validationResult.message, Toast.LENGTH_SHORT).show()
                false
            }
            is ValidationResult.Success -> true
        }
    }

    private fun verifyOtp(otpVerifyRequest: OtpVerifyRequest) {
        ApiHelper().callApi(
            context = this,
            call = userService.verifyOtp(otpVerifyRequest),
            onSuccess = {
                signUp(email, password)
            }
        )
    }

    private fun startResendTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                bindingVerify.tvExpireOTP.text = "Resend code in $seconds s"
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                bindingVerify.tvExpireOTP.text = "Over Time!"
                bindingVerify.btnSendCode.isEnabled = true
                otpCode = ""
            }
        }.start()
    }
}