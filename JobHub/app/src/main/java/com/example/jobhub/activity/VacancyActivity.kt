package com.example.jobhub.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.example.jobhub.adapter.FragmentPagerAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityVacancyBinding
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.fragment.CompanyJobFragment
import com.example.jobhub.fragment.JobDetailFragment
import com.example.jobhub.fragment.RequirementsFragment
import com.example.jobhub.fragment.fragmentinterface.FragmentInterface
import com.example.jobhub.service.UserService

class VacancyActivity : BaseActivity(), FragmentInterface {

    private lateinit var binding: ActivityVacancyBinding
    private lateinit var fragmentPagerAdapter: FragmentPagerAdapter

    private var userDTO: UserDTO? = null
    private var currentPosition = 0

    private val fragments = listOf(
        JobDetailFragment(),
        RequirementsFragment(),
        CompanyJobFragment()
    )
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVacancyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnComeBack.setOnClickListener { finish() }
        binding.btnEdit.setOnClickListener{ onEditClicked() }

        getAuthToken()?.let { decrypteken(it) } ?: Log.e("VacancyActivity", "Invalid or empty token")
        setupViewPager()
        setupCategorySelection()
    }

    private fun setupViewPager() {
        fragmentPagerAdapter = FragmentPagerAdapter(this, fragments)
        binding.viewPager.adapter = fragmentPagerAdapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                fragmentPagerAdapter.updateCurrentPosition(position)
                updateEditButtonVisibility()
                updateCategorySelection(position)
            }
        })
    }

    private fun updateEditButtonVisibility() {
        binding.btnEdit.visibility = if (userDTO?.role == Role.EMPLOYER) {
            View.VISIBLE
        } else {
            View.GONE
        }
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
        binding.btnEdit.visibility = if (userDTO?.role == Role.EMPLOYER) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun setupCategorySelection() {
        val categoryTextViews = listOf(
            binding.tvJobDetail,
            binding.tvRequirementJob,
            binding.tvCompany
        )

        categoryTextViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                if (index != currentPosition) {
                    if (index > currentPosition) {
                        binding.viewPager.setCurrentItem(index, true)
                    } else {
                        binding.viewPager.setCurrentItem(index, true)
                    }
                }
            }
        }
    }

    private fun updateCategorySelection(position: Int) {
        val categoryTextViews = listOf(
            binding.tvJobDetail,
            binding.tvRequirementJob,
            binding.tvCompany
        )

        categoryTextViews.forEachIndexed { index, textView ->
            textView.isSelected = index == position
        }
    }

    override fun onEditClicked() {
        if (userDTO?.role == Role.EMPLOYER) {
            fragments.forEach { fragment ->
                when (fragment) {
                    is JobDetailFragment -> fragment.enableEditing()
                    is RequirementsFragment -> fragment.enableEditing()
                    is CompanyJobFragment -> fragment.enableEditing()
                }
            }
        }
    }
}