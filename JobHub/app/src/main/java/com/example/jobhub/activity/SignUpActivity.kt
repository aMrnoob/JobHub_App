package com.example.jobhub.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.CreateAccountBinding
import com.example.jobhub.dto.auth.Register_ResetPwdRequest
import com.example.jobhub.model.ApiResponse
import com.example.jobhub.service.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : BaseActivity() {

    private lateinit var binding: CreateAccountBinding
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.invisiblePwd.setOnClickListener {
            togglePasswordVisibility(true)
        }

        binding.invisibleConfirmPwd.setOnClickListener {
            togglePasswordVisibility(false)
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPwd = binding.edtConfirmPwd.text.toString()

            if (email.isBlank()) {
                Toast.makeText(this@SignUpActivity, "Please enter your email.", Toast.LENGTH_SHORT).show()
            } else if(password.isBlank()) {
                Toast.makeText(this@SignUpActivity, "Please enter your password", Toast.LENGTH_SHORT).show()
            } else if(password != confirmPwd) {
                Toast.makeText(this@SignUpActivity, "Confirm password does not match.", Toast.LENGTH_SHORT).show()
            } else {
                signUp(email,password)
            }
        }

        binding.btnHaveAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnGoogle.setOnClickListener {

        }

        binding.btnFacebook.setOnClickListener {

        }
    }

    private fun signUp(email: String, password: String) {
        ApiHelper().callApi(
            context = this,
            call = userService.register(Register_ResetPwdRequest(email, password)),
            onSuccess = { }
        )
    }

    private fun togglePasswordVisibility(isPasswordField: Boolean) {

        if (isPasswordField) {
            if (isPasswordVisible) {
                binding.edtPassword.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.invisiblePwd.setImageResource(R.drawable.invisibility_icon)
            } else {
                binding.edtPassword.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.invisiblePwd.setImageResource(R.drawable.visibility_icon)
            }
            binding.edtPassword.setSelection(binding.edtPassword.text.length)
            binding.edtPassword.transformationMethod = binding.edtPassword.transformationMethod
            isPasswordVisible = !isPasswordVisible
        } else {
            if (isConfirmPasswordVisible) {
                binding.edtConfirmPwd.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.invisibleConfirmPwd.setImageResource(R.drawable.invisibility_icon)
            } else {
                binding.edtConfirmPwd.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.invisibleConfirmPwd.setImageResource(R.drawable.visibility_icon)
            }
            binding.edtConfirmPwd.setSelection(binding.edtConfirmPwd.text.length)
            binding.edtConfirmPwd.transformationMethod = binding.edtConfirmPwd.transformationMethod
            isConfirmPasswordVisible = !isConfirmPasswordVisible
        }
    }
}