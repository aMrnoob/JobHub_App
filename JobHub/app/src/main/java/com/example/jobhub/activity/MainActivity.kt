package com.example.jobhub.activity

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.jobhub.adapter.FragmentPagerAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
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
import com.google.gson.Gson

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPagerAdapter: FragmentPagerAdapter
    private lateinit var sharedPrefs: SharedPrefsManager

    private var userDTO: UserDTO? = null
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefs = SharedPrefsManager(this)

        sharedPrefs.authToken?.let { decrypteken(it) } ?: Log.e("MainActivity", "Invalid or empty token")
    }

    private fun decrypteken(token: String) {
        ApiHelper().callApi(
            context = this,
            call = userService.getUser("Bearer $token"),
            onSuccess = {
                userDTO = it
                val userJson = Gson().toJson(userDTO)
                sharedPrefs.saveCurrentUser(userJson)

                runOnUiThread { setupViewPager() }
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
    }

    private fun setupViewPager() {
        val fragments = mutableListOf(
            HomeFragment(),
            if (userDTO?.role == Role.EMPLOYER) ApplicationEmployerFragment() else ApplicationJobSeekerFragment(),
            if (userDTO?.role == Role.EMPLOYER) CompanyFragment() else ApplyFragment(),
            ProfileFragment()
        )

        viewPagerAdapter = FragmentPagerAdapter(this, fragments)
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.isUserInputEnabled = true

        setupBottomNavigation()
        setupAnimation()
    }

    private fun setupAnimation() {
        binding.tvHome.setOnClickListener {
            animateView(it)
            binding.viewPager.currentItem = 0
        }

        binding.tvApplication.setOnClickListener {
            animateView(it)
            binding.viewPager.currentItem = 1
        }

        binding.tvApply.setOnClickListener {
            animateView(it)
            binding.viewPager.currentItem = 2
        }

        binding.tvCompany.setOnClickListener {
            animateView(it)
            binding.viewPager.currentItem = 2
        }

        binding.tvProfile.setOnClickListener {
            animateView(it)
            binding.viewPager.currentItem = 3
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