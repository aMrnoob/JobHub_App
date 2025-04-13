package com.example.jobhub.activity

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.example.jobhub.adapter.FragmentPagerAdapter
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.ActivityVacancyBinding
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.fragment.CompanyJobFragment
import com.example.jobhub.fragment.JobDetailFragment
import com.example.jobhub.fragment.RequirementsFragment
import com.example.jobhub.fragment.fragmentinterface.FragmentInterface

class VacancyActivity : BaseActivity(), FragmentInterface {

    private lateinit var binding: ActivityVacancyBinding
    private lateinit var fragmentPagerAdapter: FragmentPagerAdapter
    private lateinit var sharedPrefs: SharedPrefsManager

    private var role: Role? = null
    private var currentPosition = 0

    private val fragments = listOf(
        JobDetailFragment(),
        RequirementsFragment(),
        CompanyJobFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVacancyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefs = SharedPrefsManager(this)

        binding.btnComeBack.setOnClickListener { finish() }
        binding.btnEdit.setOnClickListener{ onEditClicked() }

        role = sharedPrefs.role
        setupBottomNavigation()
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
        binding.btnEdit.visibility = if (role == Role.EMPLOYER) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun setupBottomNavigation() {
        binding.btnEdit.visibility = if (role == Role.EMPLOYER) {
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
        if (role == Role.EMPLOYER) {
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