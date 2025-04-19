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
import com.example.jobhub.activity.ApplyJobActivity
import com.example.jobhub.activity.InforJobActivity
import com.example.jobhub.adapter.JobAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.MainApplicationJobSeekerBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.entity.enumm.JobAction
import com.example.jobhub.service.JobService
import com.google.gson.Gson

class ApplicationJobSeekerFragment : Fragment() {

    private lateinit var sharedPrefs: SharedPrefsManager
    private lateinit var jobAdapter: JobAdapter

    private var userDTO: UserDTO? = null
    private var _binding: MainApplicationJobSeekerBinding? = null

    private val binding get() = _binding!!
    private val jobService: JobService by lazy { RetrofitClient.createRetrofit().create(JobService::class.java) }
    private val originalJobs: MutableList<ItemJobDTO> = mutableListOf()
    private val filteredJobs: MutableList<ItemJobDTO> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainApplicationJobSeekerBinding.inflate(inflater, container, false)
        sharedPrefs = SharedPrefsManager(requireContext())

        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()

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
                    JobAction.CLICK -> {
                        sharedPrefs.saveCurrentJob(selectedJob)
                        startActivity(Intent(requireContext(), InforJobActivity::class.java))
                    }

                    JobAction.BOOKMARK -> {
                        // Bookmark logic
                    }

                    JobAction.APPLY -> {
                        val intent = Intent(requireContext(), ApplyJobActivity::class.java)
                        sharedPrefs.saveCurrentJob(selectedJob)
                        startActivity(intent)
                    }

                    else -> {}
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

        ApiHelper().callApi(
            context = requireContext(),
            call = jobService.getAllJobsByUser("Bearer $token"),
            onSuccess = { response ->
                originalJobs.clear()
                response?.let { originalJobs.addAll(it) }
                if (binding.searchView.query.isNullOrEmpty()) {
                    filteredJobs.clear()
                    filteredJobs.addAll(originalJobs)
                }

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