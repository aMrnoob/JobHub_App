package com.example.jobhub.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.activity.VacancyActivity
import com.example.jobhub.adapter.JobAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.MainApplicationJobSeekerBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.entity.enumm.ActionType
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.service.JobService
import com.google.gson.Gson
import java.time.LocalDateTime

class ApplicationJobSeekerFragment : Fragment() {

    private var _binding: MainApplicationJobSeekerBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefs: SharedPrefsManager

    private val jobService: JobService by lazy { RetrofitClient.createRetrofit().create(JobService::class.java) }

    private val originalJobs: MutableList<ItemJobDTO> = mutableListOf()
    private val filteredJobs: MutableList<ItemJobDTO> = mutableListOf()

    private lateinit var jobAdapter: JobAdapter
    private var userDTO: UserDTO? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainApplicationJobSeekerBinding.inflate(inflater, container, false)
        sharedPrefs = SharedPrefsManager(requireContext())

        loadUserFromPrefs()
        setupRecyclerView()
        loadAppliedJobs()
        setupSearchView()

        return binding.root
    }

    private fun loadUserFromPrefs() {
        val json = activity?.getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
            ?.getString("currentUser", null)

        if (!json.isNullOrEmpty()) {
            userDTO = Gson().fromJson(json, UserDTO::class.java)
        }
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(
            filteredJobs,
            onActionClick = { selectedJob, action ->
                when (action) {
                    ActionType.CLICK -> {
                        val intent = Intent(requireContext(), VacancyActivity::class.java)
                        sharedPrefs.saveCurrentJob(selectedJob)
                        startActivity(intent)
                    }

                    ActionType.BOOKMARK -> {

                    }

                    ActionType.APPLY -> {

                    } else -> {}
                }
            }
        )

        binding.rvApplications.apply {
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadAppliedJobs() {
        val token = sharedPrefs.authToken ?: return
        val currentRole = sharedPrefs.role

        ApiHelper().callApi(
            context = requireContext(),
            call = jobService.getAllJobsByUser("Bearer $token"),
            onSuccess = { response ->
                originalJobs.clear()
                val jobs = response?.let {
                    if (currentRole == Role.JOB_SEEKER) {
                        it.filter { job -> job.expirationDate.isAfter(LocalDateTime.now()) }
                    } else {
                        it
                    }
                } ?: emptyList()
                originalJobs.addAll(jobs)

                filteredJobs.clear()
                filteredJobs.addAll(originalJobs)

                jobAdapter.notifyDataSetChanged()
            },
        )
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterJobs(newText.orEmpty())
                return true
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterJobs(query: String) {
        if (query.isEmpty()) {
            filteredJobs.clear()
            filteredJobs.addAll(originalJobs)
            jobAdapter.notifyDataSetChanged()
            binding.tvNoResults.visibility = View.GONE
            return
        }

        val filteredList = originalJobs.filter { job ->
            job.title.contains(query, ignoreCase = true) || job.location.contains(query, ignoreCase = true)
        }.toMutableList()

        filteredJobs.clear()
        filteredJobs.addAll(filteredList)
        jobAdapter.notifyDataSetChanged()

        binding.tvNoResults.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }
}
