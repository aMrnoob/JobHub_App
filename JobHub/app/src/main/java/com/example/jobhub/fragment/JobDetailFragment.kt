package com.example.jobhub.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainJobDetailBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.entity.Job
import com.example.jobhub.fragment.fragmentinterface.FragmentInterface
import com.example.jobhub.service.JobService
import com.google.gson.Gson

class JobDetailFragment : Fragment() {

    private var _binding: MainJobDetailBinding? = null
    private val binding get() = _binding!!
    private var jobDTO: ItemJobDTO? = null
    private var jobDetailInterface: FragmentInterface? = null
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
        val jobJson = sharedPreferences.getString("job", null)

        if (!jobJson.isNullOrEmpty()) {
            jobDTO = Gson().fromJson(jobJson, ItemJobDTO::class.java)
        }

        displayJob()
        setEditTextEnabled(false)
        binding.btnUpdate.setOnClickListener {
            setEditTextEnabled(false)
            update()
        }

        return binding.root
    }

    private fun displayJob() {
        jobDTO?.let {
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
        binding.btnUpdate.isEnabled = enabled
    }

    private fun update() {
        val job = Job()
        if (jobDTO == null) {
            return
        }

        job.jobId = jobDTO!!.jobId

        job.apply {
            title = binding.edtTitle.text.toString().trim()
            description = binding.edtDescription.text.toString().trim()
            requirements = binding.edtRequirements.text.toString().trim()
            salary = binding.edtSalary.text.toString().trim()
            location = binding.edtLocation.text.toString().trim()
        }

        ApiHelper().callApi(
            context = requireContext(),
            call = jobService.updateJob(job),
            onSuccess = {
                jobDTO?.apply {
                    title = job.title.toString()
                    description = job.description.toString()
                    requirements = job.requirements.toString()
                    salary = job.salary.toString()
                    location = job.location.toString()
                }
                saveJobToPrefs()
                refreshJob()
            }
        )
    }

    private fun refreshJob() {
        val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val jobJson = sharedPreferences.getString("job", null)
        if (!jobJson.isNullOrEmpty()) {
            jobDTO = Gson().fromJson(jobJson, ItemJobDTO::class.java)
            displayJob()
        }
    }

    private fun saveJobToPrefs() {
        val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val jobJson = Gson().toJson(jobDTO)
        sharedPreferences.edit().putString("job", jobJson).apply()
    }
}