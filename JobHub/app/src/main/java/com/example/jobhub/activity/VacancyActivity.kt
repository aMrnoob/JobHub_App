package com.example.jobhub.activity

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityVacancyBinding
import com.example.jobhub.dto.admin.UserInfo
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.fragment.CompanyJobFragment
import com.example.jobhub.fragment.JobDetailFragment
import com.example.jobhub.fragment.RequirementsFragment
import com.example.jobhub.fragment.fragmentinterface.FragmentInterface
import com.example.jobhub.service.UserService

class VacancyActivity : BaseActivity(), FragmentInterface {

    private lateinit var binding: ActivityVacancyBinding

    private var userInfo: UserInfo? = null
    private var jobDetailFragment: JobDetailFragment? = null
    private var requirementsFragment: RequirementsFragment? = null
    private var companyJobFragment: CompanyJobFragment? = null
    private var currentFragment: Fragment? = null
    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVacancyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnComeBack.setOnClickListener {
            finish()
        }

        getAuthToken()?.let { decryptedToken(it) } ?: Log.e("VacancyActivity", "Invalid or empty token")
        setupCategorySelection()
        setupAnimation()
    }

    private fun getAuthToken(): String? {
        return getSharedPreferences("JobHubPrefs", MODE_PRIVATE)
            .getString("authToken", null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun decryptedToken(token: String) {
        ApiHelper().callApi(
            context = this,
            call = userService.getUserInfo("Bearer $token"),
            onSuccess = {
                userInfo = it
                runOnUiThread { setupBottomNavigation() }
            }
        )
    }

    private fun setupBottomNavigation() {
        if (userInfo?.role == Role.EMPLOYER) {
            binding.btnEdit.visibility = View.VISIBLE
        } else {
            binding.btnEdit.visibility = View.GONE
        }

        jobDetailFragment = JobDetailFragment()
        loadFragment(JobDetailFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        currentFragment = fragment

        when (fragment) {
            is RequirementsFragment -> requirementsFragment = fragment
            is JobDetailFragment -> jobDetailFragment = fragment
            is CompanyJobFragment -> companyJobFragment = fragment
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setupAnimation() {
        binding.btnComeBack.setOnClickListener {
            animateView(it) { finish() }
        }

        binding.btnEdit.setOnClickListener {
            animateView(it) {
                when {
                    jobDetailFragment?.isVisible == true -> jobDetailFragment?.enableEditing()
                    requirementsFragment?.isVisible == true -> requirementsFragment?.enableEditing()
                    companyJobFragment?.isVisible == true -> companyJobFragment?.enableEditing()
                }
            }
        }
    }

    private fun animateView(view: View, onEnd: () -> Unit) {
        ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f)
        ).apply {
            duration = 200
            addListener(onEnd = { onEnd() })
            start()
        }
    }

    private fun setupCategorySelection() {
        val categoryTextViews = listOf(
            binding.tvJobDetail,
            binding.tvRequirementJob,
            binding.tvCompany
        )

        updateSelectedCategory(binding.tvJobDetail, categoryTextViews)

        categoryTextViews.forEach { textView ->
            textView.setOnClickListener {
                updateSelectedCategory(textView, categoryTextViews)

                binding.btnEdit.visibility = if (userInfo?.role == Role.EMPLOYER) View.VISIBLE else View.GONE

                val fragment = when (textView) {
                    binding.tvJobDetail -> {
                        if (jobDetailFragment == null) {
                            jobDetailFragment = JobDetailFragment()
                        }
                        jobDetailFragment!!
                    }
                    binding.tvRequirementJob -> {
                        if (requirementsFragment == null) {
                            requirementsFragment = RequirementsFragment()
                        }
                        requirementsFragment!!
                    }
                    binding.tvCompany -> {
                        if (companyJobFragment == null) {
                            companyJobFragment = CompanyJobFragment()
                        }
                        companyJobFragment!!
                    }
                    else -> return@setOnClickListener
                }

                loadFragment(fragment)
            }
        }
    }

    private fun updateSelectedCategory(selected: TextView, categoryTextViews: List<TextView>) {
        categoryTextViews.forEach { it.isSelected = it == selected }
    }

    override fun onEditClicked() {
        when {
            jobDetailFragment?.isVisible == true -> jobDetailFragment?.enableEditing()
            requirementsFragment?.isVisible == true -> requirementsFragment?.enableEditing()
            companyJobFragment?.isVisible == true -> companyJobFragment?.enableEditing()
        }
    }
}