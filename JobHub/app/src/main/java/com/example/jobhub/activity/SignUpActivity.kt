package com.example.jobhub.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.ApiService
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.TokenRequest
import com.example.jobhub.databinding.CreateAccountBinding
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

    private lateinit var binding: CreateAccountBinding
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }
    private val apiService: ApiService by lazy {
        RetrofitClient.createRetrofit().create(ApiService::class.java)
    }
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private lateinit var googleSignInClient: GoogleSignInClient

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

            if (!validateField(validateEmail(email))) return@setOnClickListener
            if (!validateField(validatePassword(password))) return@setOnClickListener
            if (password != confirmPwd) {
                Toast.makeText(this@SignUpActivity, "Confirm password does not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signUp(email,password)
        }

        binding.btnHaveAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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

        binding.btnFacebook.setOnClickListener {

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
}