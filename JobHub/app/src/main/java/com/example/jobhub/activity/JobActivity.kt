package com.example.jobhub.activity

import android.os.Bundle
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityJobBinding
import com.example.jobhub.service.JobService

class JobActivity : BaseActivity() {

    private lateinit var binding: ActivityJobBinding
    private val jobService: JobService by lazy {
        RetrofitClient.createRetrofit().create(JobService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJobBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}