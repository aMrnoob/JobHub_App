package com.example.jobhub

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.LoginScreenBinding
import com.example.jobhub.dto.auth.LoginRequest
import com.example.jobhub.dto.auth.LoginResponse
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.model.ApiResponse
import com.example.jobhub.service.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {

    private lateinit var binding: LoginScreenBinding
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.invisiblePwd.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.btnForgetPwd.setOnClickListener {
            val intent = Intent(this, ForgetPwdActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            if(email.isBlank()) {
                Toast.makeText(this@LoginActivity, "Please enter your email.", Toast.LENGTH_SHORT).show()
            } else if (password.isBlank()){
                Toast.makeText(this@LoginActivity, "Please enter your password", Toast.LENGTH_SHORT).show()
            } else {
                login(email, password)
            }
        }

        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)
        userService.login(loginRequest).enqueue(object : Callback<ApiResponse<LoginResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<LoginResponse>>,
                response: Response<ApiResponse<LoginResponse>>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@LoginActivity, response.body()?.message ?: "Login successfully", Toast.LENGTH_SHORT).show()

                    val loginResponse = response.body()?.data

                    loginResponse?.token?.let { token ->
                        saveToken(token)
                    }

                    if(loginResponse?.role == Role.UNDEFINED) {
                        val intent = Intent(this@LoginActivity, SelectProfileActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@LoginActivity, response.body()?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, response.body()?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<LoginResponse>>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("JobHubPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("authToken", token).apply()
    }

    private fun togglePasswordVisibility() {

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
    }
}