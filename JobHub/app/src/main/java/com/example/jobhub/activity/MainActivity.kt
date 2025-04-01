package com.example.jobhub.activity

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityMainBinding
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.fragment.ApplicationEmployerFragment
import com.example.jobhub.fragment.ApplicationJobSeekerFragment
import com.example.jobhub.fragment.ApplyFragment
import com.example.jobhub.fragment.CompanyFragment
import com.example.jobhub.fragment.HomeFragment
import com.example.jobhub.fragment.ProfileFragment
import com.example.jobhub.service.UserService

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private var userDTO: UserDTO? = null
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAuthToken()?.let { decrypteken(it) } ?: Log.e("MainActivity", "Invalid or empty token")
        setupBottomNavigation()

        setupAnimation()
    }

    private fun getAuthToken(): String? {
        return getSharedPreferences("JobHubPrefs", MODE_PRIVATE)
            .getString("authToken", null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun decrypteken(token: String) {
        ApiHelper().callApi(
            context = this,
            call = userService.getUser("Bearer $token"),
            onSuccess = {
                userDTO = it
                runOnUiThread { setupBottomNavigation() }
            }
        )
    }

    private fun setupBottomNavigation() {
        if (userDTO?.role == Role.EMPLOYER) {
            binding.tvApply.visibility = View.GONE
            binding.tvCompany.visibility = View.VISIBLE
        } else {
            binding.tvApply.visibility = View.VISIBLE
            binding.tvCompany.visibility = View.GONE
        }

        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setupAnimation() {
        listOf(binding.tvHome, binding.tvApplication, binding.tvApply, binding.tvCompany, binding.tvProfile)
            .forEach { view ->
                view.setOnClickListener {
                    animateView(it)
                    val fragment = when (it) {
                        binding.tvHome -> HomeFragment()
                        binding.tvApplication -> {
                            if (userDTO?.role == Role.EMPLOYER) {
                                ApplicationEmployerFragment()
                            } else {
                                ApplicationJobSeekerFragment()
                            }
                        }
                        binding.tvApply -> ApplyFragment()
                        binding.tvCompany -> CompanyFragment()
                        binding.tvProfile -> ProfileFragment()
                        else -> return@setOnClickListener
                    }

                    loadFragment(fragment)
                }
            }
    }

    private fun animateView(view: View) {
        ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f)
        ).apply {
            duration = 300
            start()
        }
    }
}