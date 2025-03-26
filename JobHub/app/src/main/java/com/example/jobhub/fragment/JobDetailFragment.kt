package com.example.jobhub.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainJobDetailBinding
import com.example.jobhub.dto.employer.JobInfo
import com.example.jobhub.fragment.fragmentinterface.FragmentInterface
import com.example.jobhub.service.JobService
import com.google.gson.Gson

class JobDetailFragment : Fragment() {

    private var _binding: MainJobDetailBinding? = null
    private val binding get() = _binding!!
    private var jobInfo: JobInfo? = null
    private var jobDetailInterface: FragmentInterface? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val jobService: JobService by lazy {
        RetrofitClient.createRetrofit().create(JobService::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentInterface) {
            jobDetailInterface = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainJobDetailBinding.inflate(inflater, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val jobInfoJson = sharedPreferences.getString("job_info", null)

        if (!jobInfoJson.isNullOrEmpty()) {
            jobInfo = Gson().fromJson(jobInfoJson, JobInfo::class.java)
        }

        displayJobInfo()
        setEditTextEnabled(false)
        binding.btnUpdate.setOnClickListener {
            setEditTextEnabled(false)
            update()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(refreshRunnable, 10000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun displayJobInfo() {
        jobInfo?.let {
            binding.edtTitle.setText(it.title)
            binding.edtDescription.setText(it.description)
            binding.edtRequirements.setText(it.requirements)
            binding.edtSalary.setText(it.salary)
            binding.edtLocation.setText(it.location)
        }
    }

    fun enableEditing() {
        setEditTextEnabled(true)
    }

    private fun setEditTextEnabled(enabled: Boolean) {
        binding.edtTitle.isEnabled = enabled
        binding.edtDescription.isEnabled = enabled
        binding.edtRequirements.isEnabled = enabled
        binding.edtSalary.isEnabled = enabled
        binding.edtLocation.isEnabled = enabled
    }

    private fun update() {
        if (jobInfo == null) return
        jobInfo?.apply {
            title = binding.edtTitle.text.toString().trim()
            description = binding.edtDescription.text.toString().trim()
            requirements = binding.edtRequirements.text.toString().trim()
            salary = binding.edtSalary.text.toString().trim()
            location = binding.edtLocation.text.toString().trim()
        }

        ApiHelper().callApi(
            context = requireContext(),
            call = jobService.updateJob(jobInfo!!),
            onSuccess = {
                saveJobInfoToPrefs()
                displayJobInfo()
            }
        )
    }

    private val refreshRunnable = object : Runnable {
        override fun run() {
            refreshJobInfo()
            handler.postDelayed(this, 10000)
        }
    }

    private fun refreshJobInfo() {
        val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val jobInfoJson = sharedPreferences.getString("job_info", null)

        if (!jobInfoJson.isNullOrEmpty()) {
            jobInfo = Gson().fromJson(jobInfoJson, JobInfo::class.java)
            displayJobInfo()
        }
    }

    private fun saveJobInfoToPrefs() {
        val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val jobJson = Gson().toJson(jobInfo)
        sharedPreferences.edit().putString("job_info", jobJson).apply()
    }
}