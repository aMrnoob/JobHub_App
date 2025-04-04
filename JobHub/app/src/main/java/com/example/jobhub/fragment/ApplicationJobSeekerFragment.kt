package com.example.jobhub.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.activity.VacancyActivity
import com.example.jobhub.adapter.JobAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainApplicationJobSeekerBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.service.JobService
import com.google.gson.Gson
import java.time.LocalDateTime

class ApplicationJobSeekerFragment : Fragment() {

    private var _binding: MainApplicationJobSeekerBinding? = null
    private val binding get() = _binding!!

    private val jobService: JobService by lazy {
        RetrofitClient.createRetrofit().create(JobService::class.java)
    }
    private var appliedJobs: MutableList<ItemJobDTO> = mutableListOf()
    private lateinit var jobAdapter: JobAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainApplicationJobSeekerBinding.inflate(inflater, container, false)

        setupRecyclerView()
        getAppliedJobs()
        setupFilters()

        return binding.root
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(appliedJobs) { selectedJob ->
            val intent = Intent(requireContext(), VacancyActivity::class.java)
            val jobJson = Gson().toJson(selectedJob)
            val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("job", jobJson).apply()
            startActivity(intent)
        }

        binding.rvApplications.apply {
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun getAppliedJobs() {
        val token = getAuthToken() ?: return

        ApiHelper().callApi(
            context = requireContext(),
            call = jobService.getAllJobsByUser("Bearer $token"),
            onSuccess = { response ->
                appliedJobs.apply {
                    clear()
                    response?.let { addAll(it) }
                }
                jobAdapter.notifyDataSetChanged()
            },
        )
    }

    private fun getAuthToken(): String? {
        val sharedPreferences = activity?.getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("authToken", null)?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun setupFilters() {
        binding.tvAllVacancies.setOnClickListener { filterJobs("all") }
        binding.tvActive.setOnClickListener { filterJobs("active") }
        binding.tvInactive.setOnClickListener { filterJobs("expired") }
    }

    private fun filterJobs(status: String) {
        val filteredList = appliedJobs.filter { job ->
            val isExpired = job.expirationDate.isBefore(LocalDateTime.now())
            when (status) {
                "active" -> !isExpired
                "expired" -> isExpired
                else -> true
            }
        }
        jobAdapter.updateList(filteredList)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
