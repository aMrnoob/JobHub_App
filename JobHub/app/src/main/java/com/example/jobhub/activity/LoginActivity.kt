package com.example.jobhub.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.jobhub.R
import com.example.jobhub.anim.AnimationHelper
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
    private var callbackManager: CallbackManager? = null
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

        FacebookSdk.fullyInitialize()
        callbackManager = CallbackManager.Factory.create()

        if (sharedPrefs.isRemembered) {
            binding.edtEmail.setText(sharedPrefs.email)
            binding.edtPassword.setText(sharedPrefs.password)
            binding.cbRememberPassword.isChecked = true
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.invisiblePwd.setOnClickListener {
            AnimationHelper.animateScale(it)
            togglePasswordVisibility()
        }

        binding.btnGoogle.setOnClickListener {
            AnimationHelper.animateScale(it)
            initGoogleSignIn()
        }

        binding.btnFacebook.setOnClickListener {
            AnimationHelper.animateScale(it)
            initFacebookLogin()
        }

        binding.btnForgetPwd.setOnClickListener {
            AnimationHelper.animateScale(it)
            startActivity(Intent(this, ForgetPwdActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            AnimationHelper.animateScale(it)
            handleLogin()
        }

        binding.btnRegister.setOnClickListener {
            AnimationHelper.animateScale(it)
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("264261988951-sc4t0kkmjbi2vnlr0c9g2jpu3tv3egcl.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            try {
                signInLauncher.launch(signInIntent)
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Error launching sign in: ${e.message}")
                Toast.makeText(this, "Error launching Google Sign In", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initFacebookLogin() {
        if (callbackManager == null) {
            callbackManager = CallbackManager.Factory.create()
        }

        LoginManager.getInstance().logOut()
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager!!,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val accessToken = result.accessToken
                    getFacebookId(accessToken)
                }

                override fun onCancel() {
                    Toast.makeText(this@LoginActivity, "Facebook login canceled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@LoginActivity, "Facebook login error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun handleLogin() {
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPassword.text.toString()

        if (email.isBlank()) {
            Toast.makeText(this@LoginActivity, "Please enter your email.", Toast.LENGTH_SHORT).show()
        } else if (password.isBlank()) {
            Toast.makeText(this@LoginActivity, "Please enter your password", Toast.LENGTH_SHORT).show()
        } else {
            if (binding.cbRememberPassword.isChecked) {
                sharedPrefs.email = email
                sharedPrefs.password = password
                sharedPrefs.isRemembered = true
            } else {
                sharedPrefs.clearRemembered()
            }
            login(email, password)
        }
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                Log.d("GoogleSignIn", "ID Token: $idToken")

                if (idToken != null) {
                    sendTokenToBackend(idToken)
                } else {
                    Toast.makeText(this, "Failed to get ID token", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Login failed: ${e.statusCode}, ${e.message}")
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("GoogleSignIn", "Sign-in canceled. Result code: ${result.resultCode}")
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

    private fun sendTokenToBackend(idToken: String) {
        val request = TokenRequest(idToken)

        binding.progressBar.visibility = View.VISIBLE
        ApiHelper().callApi(
            context = this,
            call = apiService.sendGoogleToken(request),
            onStart = { binding.progressBar.visibility = View.VISIBLE },
            onComplete = { binding.progressBar.visibility = View.GONE },
            onSuccess = { apiResponse ->
                apiResponse?.let {
                    Log.d("GoogleAuth", "Token response: $it")
                    sharedPrefs.authToken = it.token
                    sharedPrefs.userId = it.userId
                    sharedPrefs.role = it.role
                    sharedPrefs.fullName = it.fullName

                    navigateToNextScreen(it.role)
                    finish()
                }
            },
            onError = { errorMessage ->
                Log.e("GoogleAuth", "Error: $errorMessage")
                Toast.makeText(this, "Authentication error: $errorMessage", Toast.LENGTH_SHORT).show()
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
                loginResponse?.let {
                    sharedPrefs.authToken = it.token
                    sharedPrefs.userId = it.userId
                    sharedPrefs.role = it.role
                    sharedPrefs.fullName = it.fullName
                    navigateToNextScreen(it.role)
                    finish()
                }
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
            else -> SelectProfileActivity::class.java
        }
        startActivity(Intent(this, nextActivity))
    }

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

    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }
}