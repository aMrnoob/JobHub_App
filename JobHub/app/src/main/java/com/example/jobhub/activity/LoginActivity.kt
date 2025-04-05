package com.example.jobhub.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.ApiService
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.TokenRequest
import com.example.jobhub.databinding.LoginScreenBinding
import com.example.jobhub.dto.auth.LoginRequest
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.service.UserService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : BaseActivity() {

    private lateinit var binding: LoginScreenBinding
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }
    private val apiService: ApiService by lazy {
        RetrofitClient.createRetrofit().create(ApiService::class.java)
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
                Log.w("GoogleSignIn", "Login failed: ${e.statusCode}")
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

    private fun login(email: String, password: String) {
        ApiHelper().callApi(
            context = this,
            call = userService.login(LoginRequest(email, password)),
            onStart = { binding.progressBar.visibility = View.VISIBLE },
            onComplete = { binding.progressBar.visibility = View.GONE },
            onSuccess = { loginResponse ->
                loginResponse?.token?.let { saveToken(it) }
                navigateToNextScreen(loginResponse?.role)
            }
        )
    }

    private fun navigateToNextScreen(role: Role?) {
        val nextActivity = when (role) {
            Role.UNDEFINED -> SelectProfileActivity::class.java
            Role.EMPLOYER, Role.JOB_SEEKER, Role.ADMIN -> MainActivity::class.java
            else -> null
        }
        nextActivity?.let { startActivity(Intent(this, it)) }
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