package com.example.jobhub.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.jobhub.R
import com.example.jobhub.adapter.FragmentPagerAdapter
import com.example.jobhub.anim.AnimationHelper
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.ActivityInforJobBinding
import com.example.jobhub.dto.BookmarkRequest
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.entity.enumm.JobType
import com.example.jobhub.fragment.InfoCompanyFragment
import com.example.jobhub.fragment.InfoJobFragment
import com.example.jobhub.service.BookmarkService
import java.util.Locale

class InforJobActivity : BaseActivity() {

    private lateinit var sharedPrefs: SharedPrefsManager
    private lateinit var binding: ActivityInforJobBinding
    private lateinit var adapter: FragmentPagerAdapter

    private var jobDTO: ItemJobDTO? = null

    private val bookmarkService: BookmarkService by lazy { RetrofitClient.createRetrofit().create(
        BookmarkService::class.java) }
    private val fragments = listOf(
        InfoJobFragment(),
        InfoCompanyFragment()
    )

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInforJobBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefs = SharedPrefsManager(this)
        jobDTO = sharedPrefs.getCurrentJob()

        binding.btnComeBack.setOnClickListener {
            AnimationHelper.animateScale(it)
            finish()
        }

        binding.tvJobTitle.text = jobDTO?.title ?: ""
        binding.tvLocation.text = jobDTO?.location ?: ""
        binding.tvSalary.text = jobDTO?.salary ?: ""
        binding.tvJobType.text = "Job Type - " + (jobDTO?.jobType?.toFriendlyString() ?: "")

        binding.ivBookMark.setOnClickListener {
            AnimationHelper.animateScale(it)
            val userId = sharedPrefs.userId
            val jobId = jobDTO?.jobId
            bookMark(BookmarkRequest(userId, jobId))
        }

        binding.tvApply.setOnClickListener {
            AnimationHelper.animateScale(it)
            val intent = Intent(this, ApplyJobActivity::class.java)
            jobDTO?.let { it1 -> sharedPrefs.saveCurrentJob(it1) }
            startActivity(intent)
        }

        setupViewPager()
        setupTabHighlighting()
    }

    private fun setupViewPager() {
        adapter = FragmentPagerAdapter(this, fragments)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                highlightTab(position)
            }
        })
    }

    private fun setupTabHighlighting() {
        binding.llJobDetail.setOnClickListener { binding.viewPager.currentItem = 0 }
        binding.llCompany.setOnClickListener { binding.viewPager.currentItem = 1 }
    }

    private fun highlightTab(position: Int) {
        val highlightColor = Color.parseColor("#FF9966")
        val defaultColor = ContextCompat.getColor(this, R.color.black)
        val defaultUnderline = Color.parseColor("#CCCCCC")

        when (position) {
            0 -> {
                binding.tvJobDetail.setTextColor(highlightColor)
                binding.vUnderlineJob.setBackgroundColor(highlightColor)
                binding.tvCompany.setTextColor(defaultColor)
                binding.vUnderlineCompany.setBackgroundColor(defaultUnderline)
            }
            1 -> {
                binding.tvCompany.setTextColor(highlightColor)
                binding.vUnderlineCompany.setBackgroundColor(highlightColor)
                binding.tvJobDetail.setTextColor(defaultColor)
                binding.vUnderlineJob.setBackgroundColor(defaultUnderline)
            }
        }
    }

    private fun bookMark(bookmarkRequest: BookmarkRequest) {
        ApiHelper().callApi(
            context = this,
            call = bookmarkService.bookMark(bookmarkRequest),
            onSuccess = { }
        )
    }

    private fun JobType.toFriendlyString(): String {
        return this.name.split("_")
            .joinToString(" ") { it.capitalize(Locale.ROOT) }
    }
}