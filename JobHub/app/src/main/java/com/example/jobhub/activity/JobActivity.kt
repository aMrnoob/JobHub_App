package com.example.jobhub.activity

import android.os.Bundle
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityAboutJobBinding
import com.example.jobhub.service.JobService

class JobActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutJobBinding
    private val jobService: JobService by lazy {
        RetrofitClient.createRetrofit().create(JobService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutJobBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}