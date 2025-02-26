package com.example.jobhub

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.CreateAccountBinding
import com.example.jobhub.dto.auth.LoginResponse
import com.example.jobhub.dto.auth.RegisterRequest
import com.example.jobhub.model.ApiResponse
import com.example.jobhub.service.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: CreateAccountBinding
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val username = binding.edtUsername.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPwd = binding.edtConfirmPwd.text.toString()

            if (email.isBlank()) {
                Toast.makeText(this@SignUpActivity, "Vui lòng điền email", Toast.LENGTH_SHORT)
            } else if (username.isBlank()) {
                Toast.makeText(this@SignUpActivity, "Vui lòng điền tên đăng nhập", Toast.LENGTH_SHORT)
            } else if(password.isBlank()) {
                Toast.makeText(this@SignUpActivity, "Vui lòng điền mật khẩu", Toast.LENGTH_SHORT)
            } else if(!password.equals(confirmPwd)) {
                Toast.makeText(this@SignUpActivity, "Xác nhận mật khẩu không đúng", Toast.LENGTH_SHORT)
            } else {
                signUp(email,username,password)

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

    private fun signUp(email: String, username: String, password: String) {
        val registerRequest = RegisterRequest(email, username, password)
        userService.register(registerRequest).enqueue(object : Callback<ApiResponse<Void>> {
            override fun onResponse(
                call: Call<ApiResponse<Void>>,
                response: Response<ApiResponse<Void>>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@SignUpActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SignUpActivity, response.body()?.message ?: "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                Toast.makeText(this@SignUpActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}