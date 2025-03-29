package com.example.jobhub.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.jobhub.R
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.TokenRequest
import com.example.jobhub.config.TokenResponse
import com.example.jobhub.databinding.LoginScreenBinding
import com.example.jobhub.dto.auth.LoginRequest
import com.example.jobhub.dto.auth.LoginResponse
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.model.ApiResponse
import com.example.jobhub.service.UserService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {

    private lateinit var binding: LoginScreenBinding
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }
    private var isPasswordVisible = false

    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.invisiblePwd.setOnClickListener {
            togglePasswordVisibility()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("856354548077-e00ibmh0ojbv416s43qldd8ec0j4o43m.apps.googleusercontent.com") // Thay bằng Web Client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Xử lý nút đăng nhập với binding
        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
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

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                Log.d("GoogleSignIn", "ID Token: $idToken")
                sendTokenToBackend(idToken)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Đăng nhập thất bại: ${e.statusCode}")
            }
        }
    }

    private fun sendTokenToBackend(idToken: String?) {
        if (idToken == null) return

        val request = TokenRequest(idToken)
        val call = RetrofitClient.apiService.sendGoogleToken(request)

        call.enqueue(object : Callback<ApiResponse<LoginResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<LoginResponse>>,
                response: Response<ApiResponse<LoginResponse>>
            ) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse != null && apiResponse.isSuccess) {
                        val loginResponse = apiResponse.data
                        Toast.makeText(this@LoginActivity, apiResponse.message ?: "Login successfully", Toast.LENGTH_SHORT).show()

                        loginResponse?.token?.let { token ->
                            saveToken(token)
                        }

                        startActivity(Intent(this@LoginActivity, SelectProfileActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, apiResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                        Log.e("GoogleSignIn", "Lỗi: ${response.code()} - ${response.message()}")
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                    Log.e("GoogleSignIn", "Lỗi: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<LoginResponse>>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("GoogleSignIn", "Gửi thất bại: ${t.message}")
            }
        })
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

                    when (loginResponse?.role) {
                        Role.UNDEFINED -> startActivity(Intent(this@LoginActivity, SelectProfileActivity::class.java))
                        Role.EMPLOYER, Role.JOB_SEEKER, Role.ADMIN -> startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        else -> Toast.makeText(this@LoginActivity, response.body()?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
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