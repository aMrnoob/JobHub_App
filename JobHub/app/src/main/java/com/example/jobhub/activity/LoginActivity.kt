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
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.config.TokenRequest
import com.example.jobhub.databinding.LoginScreenBinding
import com.example.jobhub.dto.auth.LoginRequest
import com.example.jobhub.dto.auth.Register_ResetPwdRequest
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.service.UserService
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : BaseActivity() {

    private lateinit var binding: LoginScreenBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var sharedPrefs: SharedPrefsManager

    private val userService: UserService by lazy { RetrofitClient.createRetrofit().create(UserService::class.java) }
    private val apiService: ApiService by lazy { RetrofitClient.createRetrofit().create(ApiService::class.java) }

    private var isPasswordVisible = false
    private var facebookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefs = SharedPrefsManager(this)

        if (sharedPrefs.isRemembered) {
            binding.edtEmail.setText(sharedPrefs.email)
            binding.edtPassword.setText(sharedPrefs.password)
            binding.cbRememberPassword.isChecked = true
        }

        binding.invisiblePwd.setOnClickListener { togglePasswordVisibility() }

        binding.btnGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("856354548077-e00ibmh0ojbv416s43qldd8ec0j4o43m.apps.googleusercontent.com") // Thay bằng Web Client ID
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(this, gso)

            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        }

        binding.btnFacebook.setOnClickListener {
            FacebookSdk.fullyInitialize()
            callbackManager = CallbackManager.Factory.create()

            LoginManager.getInstance().logInWithReadPermissions(this, emptyList())
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        val accessToken = result.accessToken
                        getFacebookId(accessToken)
                    }

                    override fun onCancel() { Toast.makeText(this@LoginActivity, "Facebook login canceled", Toast.LENGTH_SHORT).show() }

                    override fun onError(error: FacebookException) { Toast.makeText(this@LoginActivity, "Facebook login error: ${error.message}", Toast.LENGTH_SHORT).show() }
                })
        }

        binding.btnForgetPwd.setOnClickListener { startActivity(Intent(this, ForgetPwdActivity::class.java)) }

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            if(email.isBlank()) {
                Toast.makeText(this@LoginActivity, "Please enter your email.", Toast.LENGTH_SHORT).show()
            } else if (password.isBlank()){
                Toast.makeText(this@LoginActivity, "Please enter your password", Toast.LENGTH_SHORT).show()
            } else {
                if (binding.cbRememberPassword.isChecked) {
                    sharedPrefs.email = email
                    sharedPrefs.password = password
                    sharedPrefs.isRemembered = true
                } else {
                    sharedPrefs.clearRememberedCredentials()
                }
                login(email, password)
            }
        }

        binding.btnRegister.setOnClickListener { startActivity(Intent(this, SignUpActivity::class.java)) }
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

    private fun getFacebookId(accessToken: AccessToken) {
        val request = GraphRequest.newMeRequest(accessToken) { obj, _ ->
            val id = obj?.optString("id")

            if (!id.isNullOrBlank()) {
                facebookId = "$id@facebook.com"
                checkAccountExist(facebookId) { exist ->
                    if (exist) {
                        login(facebookId, "")
                    } else {
                        signUp(facebookId) {
                            login(facebookId, "")
                        }
                    }
                }
            }
        }

        val params = Bundle()
        params.putString("fields", "id")
        request.parameters = params
        request.executeAsync()
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
                finish()
            }
        )
    }

    private fun checkAccountExist(email: String, onResult: (Boolean) -> Unit) {
        ApiHelper().callApi(
            context = this,
            call = userService.findByEmail(email),
            onSuccess = { onResult(true) },
            onError = { onResult(false) }
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

    private fun saveToken(token: String) { sharedPrefs.authToken = token }

    private fun togglePasswordVisibility() {
        val (inputType, iconRes) = if (isPasswordVisible) {
            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD to R.drawable.invisibility_icon
        } else {
            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD to R.drawable.visibility_icon
        }

        binding.edtPassword.inputType = inputType
        binding.invisiblePwd.setImageResource(iconRes)
        binding.edtPassword.setSelection(binding.edtPassword.text.length)

        isPasswordVisible = !isPasswordVisible
    }

    private fun signUp(email: String, onComplete: () -> Unit) {
        ApiHelper().callApi(
            context = this,
            call = userService.register(Register_ResetPwdRequest(email, "")),
            onSuccess = { onComplete() }
        )
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}