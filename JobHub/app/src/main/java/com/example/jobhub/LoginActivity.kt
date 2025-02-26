package com.example.jobhub

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.LoginScreenBinding
import com.example.jobhub.dto.auth.LoginRequest
import com.example.jobhub.dto.auth.LoginResponse
import com.example.jobhub.model.ApiResponse
import com.example.jobhub.service.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginScreenBinding
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val username = binding.edtUsername.text.toString()
            val password = binding.edtPassword.text.toString()

            if(username.isBlank()) {
                Toast.makeText(this@LoginActivity, "Vui lòng điền tên đăng nhập", Toast.LENGTH_SHORT).show()
            } else if (password.isBlank()){
                Toast.makeText(this@LoginActivity, "Vui lòng điền mật khẩu", Toast.LENGTH_SHORT).show()
            } else {
                login(username, password)
            }
        }

        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(username: String, password: String) {
        val loginRequest = LoginRequest(username, password)
        userService.login(loginRequest).enqueue(object : Callback<ApiResponse<LoginResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<LoginResponse>>,
                response: Response<ApiResponse<LoginResponse>>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@LoginActivity, response.body()?.message ?: "Đăng nhập thành công", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, response.body()?.message ?: "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<LoginResponse>>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}