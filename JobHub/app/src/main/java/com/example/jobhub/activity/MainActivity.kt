package com.example.jobhub.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.jobhub.R
import com.example.jobhub.adapter.FragmentPagerAdapter
import com.example.jobhub.anim.AnimationHelper
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.ActivityMainBinding
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.fragment.ApplicationEmployerFragment
import com.example.jobhub.fragment.ApplicationJobSeekerFragment
import com.example.jobhub.fragment.ApplyFragment
import com.example.jobhub.fragment.CompanyFragment
import com.example.jobhub.fragment.HomeFragment
import com.example.jobhub.fragment.ProfileFragment

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPagerAdapter: FragmentPagerAdapter
    private lateinit var sharedPrefs: SharedPrefsManager
    private lateinit var tabPositionMap: Map<TextView, Int>

    private var role: Role? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefs = SharedPrefsManager(this)
        role = sharedPrefs.role
        setupViewPager()
    }


    private fun setupBottomNavigation() {
        if (role == Role.EMPLOYER) {
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
            if (role == Role.EMPLOYER) ApplicationEmployerFragment() else ApplicationJobSeekerFragment(),
            if (role == Role.EMPLOYER) CompanyFragment() else ApplyFragment(),
            ProfileFragment()
        )

        viewPagerAdapter = FragmentPagerAdapter(this, fragments)
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.isUserInputEnabled = true

        setupBottomNavigation()
        initTabPositionMap()
        setupAnimation()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabColorOnPageChange(position)
            }
        })
    }

    private fun updateTabColorOnPageChange(position: Int) {
        val colorSelected = ContextCompat.getColor(this, R.color.blue_600)
        val colorUnselected = ContextCompat.getColor(this, R.color.gray_500)

        tabPositionMap.forEach { (tab, pos) ->
            if (pos == position) {
                updateTabColor(tab, colorSelected, colorUnselected)
            }
        }
    }

    private fun setupAnimation() {
        tabPositionMap.forEach { (tab, position) ->
            tab.setOnClickListener {
                AnimationHelper.animateScale(it)
                binding.viewPager.currentItem = position
            }
        }
    }

    private fun updateTabColor(view: View, colorSelected: Int, colorUnselected: Int) {
        if (view is TextView) {
            view.setTextColor(colorSelected)
            changeIconColor(view, colorSelected)
        }

        val tabs = listOf(binding.tvHome, binding.tvApplication, binding.tvApply, binding.tvCompany, binding.tvProfile)
        tabs.forEach {
            if (it != view) {
                it.setTextColor(colorUnselected)
                changeIconColor(it, colorUnselected)
            }
        }
    }

    private fun initTabPositionMap() {
        tabPositionMap = if (role == Role.EMPLOYER) {
            mapOf(
                binding.tvHome to 0,
                binding.tvApplication to 1,
                binding.tvCompany to 2,
                binding.tvProfile to 3
            )
        } else {
            mapOf(
                binding.tvHome to 0,
                binding.tvApplication to 1,
                binding.tvApply to 2,
                binding.tvProfile to 3
            )
        }
    }

    private fun changeIconColor(view: TextView, color: Int) { view.compoundDrawables[1]?.setTint(color) }
}