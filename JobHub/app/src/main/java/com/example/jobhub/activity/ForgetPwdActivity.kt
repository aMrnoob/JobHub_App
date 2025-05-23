package com.example.jobhub.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.jobhub.R
import com.example.jobhub.anim.AnimationHelper
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ForgetPwdBinding
import com.example.jobhub.databinding.ResetPasswordBinding
import com.example.jobhub.databinding.VerifyOtpBinding
import com.example.jobhub.dto.auth.OtpRequest
import com.example.jobhub.dto.auth.OtpVerifyRequest
import com.example.jobhub.dto.auth.Register_ResetPwdRequest
import com.example.jobhub.service.UserService

class ForgetPwdActivity : BaseActivity() {

    private lateinit var bindingForgetPwd: ForgetPwdBinding
    private lateinit var bindingVerify: VerifyOtpBinding
    private lateinit var bindingNewPwd: ResetPasswordBinding
    private lateinit var userEmail: String
    private lateinit var otpCode: String

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private var countDownTimer: CountDownTimer? = null
    private var currentStep = 1

    private val userService: UserService by lazy { RetrofitClient.createRetrofit().create(UserService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showStep(1)
    }

    private fun showStep(step: Int) {
        when (step) {
            1 -> {
                bindingForgetPwd = ForgetPwdBinding.inflate(layoutInflater)
                setContentView(bindingForgetPwd.root)

                bindingForgetPwd.btnComeBack.setOnClickListener {
                    AnimationHelper.animateScale(it)
                    startActivity(Intent(this, LoginActivity::class.java))
                }

                bindingForgetPwd.btnConfirm.setOnClickListener {
                    userEmail = bindingForgetPwd.edtEmail.text.toString().trim()
                    if (userEmail.isEmpty()) {
                        Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                    } else {
                        AnimationHelper.animateScale(it)
                        requestOtp(OtpRequest(userEmail))
                    }
                }
            }
            2 -> {
                bindingVerify = VerifyOtpBinding.inflate(layoutInflater)
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
                    requestOtp(OtpRequest(userEmail))
                    bindingVerify.btnSendCode.isEnabled = false
                }

                bindingVerify.btnConfirm.setOnClickListener {
                    otpCode = otpFields.joinToString("") { it.text.toString() }.trim()

                    if (otpCode.isEmpty()) {
                        Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
                    } else {
                        verifyOtpAndResetPassword(OtpVerifyRequest(userEmail, otpCode))
                    }
                }
            }
            3 -> {
                bindingNewPwd = ResetPasswordBinding.inflate(layoutInflater)
                setContentView(bindingNewPwd.root)

                isPasswordVisible = false
                isConfirmPasswordVisible = false

                bindingNewPwd.btnComeBack.setOnClickListener {
                    requestOtp(OtpRequest(userEmail))
                    showStep(2)
                }

                bindingNewPwd.invisibleNewPwd.setOnClickListener {
                    togglePasswordVisibility(true)
                }

                bindingNewPwd.invisibleConfirmNewPwd.setOnClickListener {
                    togglePasswordVisibility(false)
                }

                bindingNewPwd.btnConfirm.setOnClickListener {
                    val newPassword = bindingNewPwd.edtNewPwd.text.toString().trim()
                    val confirmNewPwd = bindingNewPwd.edtConfirmNewPwd.text.toString().trim()
                    if (newPassword.isEmpty()) {
                        Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show()
                    } else if(confirmNewPwd.isBlank()) {
                        Toast.makeText(this, "Please enter your confirm password", Toast.LENGTH_SHORT).show()
                    } else if(newPassword != confirmNewPwd) {
                        Toast.makeText(this, "Confirm password does not match.", Toast.LENGTH_SHORT).show()
                    } else {
                        passwordReset(Register_ResetPwdRequest(userEmail, newPassword))
                    }
                }
            }
        }
    }

    private fun requestOtp(otpRequest: OtpRequest) {
        ApiHelper().callApi(
            context = this,
            call = userService.resetPasswordRequest(otpRequest),
            onStart = { handleProgressBar(true) },
            onComplete = { handleProgressBar(false) },
            onSuccess = {
                currentStep = 2
                showStep(currentStep)
                startResendTimer()
            }
        )
    }

    private fun verifyOtpAndResetPassword(otpVerifyRequest: OtpVerifyRequest) {
        ApiHelper().callApi(
            context = this,
            call = userService.verifyOtp(otpVerifyRequest),
            onSuccess = {
                currentStep = 3
                showStep(currentStep)
            }
        )
    }

    private fun passwordReset(resetPwdRequest: Register_ResetPwdRequest) {
        ApiHelper().callApi(
            context = this,
            call = userService.passwordReset(resetPwdRequest),
            onSuccess = {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        )
    }

    private fun togglePasswordVisibility(isPasswordField: Boolean) {

        if (isPasswordField) {
            if (isPasswordVisible) {
                bindingNewPwd.edtNewPwd.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                bindingNewPwd.invisibleNewPwd.setImageResource(R.drawable.invisibility_icon)
            } else {
                bindingNewPwd.edtNewPwd.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                bindingNewPwd.invisibleNewPwd.setImageResource(R.drawable.visibility_icon)
            }
            bindingNewPwd.edtNewPwd.setSelection(bindingNewPwd.edtNewPwd.text.length)
            bindingNewPwd.edtNewPwd.transformationMethod = bindingNewPwd.edtNewPwd.transformationMethod
            isPasswordVisible = !isPasswordVisible
        } else {
            if (isConfirmPasswordVisible) {
                bindingNewPwd.edtConfirmNewPwd.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                bindingNewPwd.invisibleConfirmNewPwd.setImageResource(R.drawable.invisibility_icon)
            } else {
                bindingNewPwd.edtConfirmNewPwd.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                bindingNewPwd.invisibleConfirmNewPwd.setImageResource(R.drawable.visibility_icon)
            }
            bindingNewPwd.edtConfirmNewPwd.setSelection(bindingNewPwd.edtConfirmNewPwd.text.length)
            bindingNewPwd.edtConfirmNewPwd.transformationMethod = bindingNewPwd.edtConfirmNewPwd.transformationMethod
            isConfirmPasswordVisible = !isConfirmPasswordVisible
        }
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

    private fun handleProgressBar(isVisible: Boolean) {
        when (currentStep) {
            1 -> bindingForgetPwd.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
            2 -> bindingVerify.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }
}