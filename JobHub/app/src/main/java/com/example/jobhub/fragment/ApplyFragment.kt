package com.example.jobhub.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.activity.ApplicationDetailsActivity
import com.example.jobhub.adapter.SeekerApplicationAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.MainApplyBinding
import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.service.ApplicationService
import com.example.jobhub.service.ResumeService
import com.google.gson.Gson

class ApplyFragment: Fragment() {
    private var _binding: MainApplyBinding? = null
    private val binding get() = _binding!!
    private var currentUser: UserDTO? = null
    private var applications = mutableListOf<ApplicationDTO>()
    private lateinit var sharedPrefs: SharedPrefsManager

    private val applicationService by lazy {
        RetrofitClient.createRetrofit().create(ApplicationService::class.java)
    }

    private val resumeService by lazy {
        RetrofitClient.createRetrofit().create(ResumeService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainApplyBinding.inflate(inflater, container, false)
        sharedPrefs = SharedPrefsManager(requireContext())
        loadUserData()
        setupRecyclerView()
        setupSwipeRefresh()
        loadApplications()
        setupEmptyStateButton()
        return binding.root
    }

    private fun loadUserData() {
        val json = activity?.getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
            ?.getString("currentUser", null)
        if (!json.isNullOrEmpty()) {
            currentUser = Gson().fromJson(json, UserDTO::class.java)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerApplications.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadApplications()
        }
    }

    private fun setupEmptyStateButton() {
        binding.btnExploreJobs.setOnClickListener {
            activity?.let { activity ->
                if (activity is OnFragmentInteractionListener) {
                    (activity as OnFragmentInteractionListener).onNavigateToJobSearch()
                } else {
                    showToast("")
                }
            }
        }
    }

    private fun loadApplications() {
        val token = getAuthToken()
        if (token == null) {
            return
        }

        if (currentUser == null) {
            showToast("")
            showEmptyState()
            return
        }

        val userId = currentUser?.userId ?: 0

        if (userId == 0) {
            showToast("")
            showEmptyState()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerApplications.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE

        ApiHelper().callApi(
            context = requireContext(),
            call = applicationService.getApplicationsByUserId("Bearer $token", userId),
            onSuccess = { result ->
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false

                if (result != null && result.isNotEmpty()) {
                    applications.clear()
                    applications.addAll(result)
                    updateUI()
                    fetchResumeURLsInBackground(token)
                } else {
                    showEmptyState()
                }
            },
            onError = { error ->
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                showToast("Không thể tải danh sách ứng tuyển: ${error ?: "Unknown error"}")
                showEmptyState()
            }
        )
    }

    private fun fetchResumeURLsInBackground(token: String) {
        if (applications.isEmpty()) {
            return
        }

        for (application in applications) {
            val applicationId = application.applicationId ?: continue

            ApiHelper().callApi(
                context = requireContext(),
                call = resumeService.getResumeByApplicationId("Bearer $token", applicationId),
                onSuccess = { resumeDTO ->
                    val updatedApplication = application.copy(resumeUrl = resumeDTO?.resumeUrl ?: "")
                    val index = applications.indexOfFirst { it.applicationId == applicationId }
                    if (index != -1) {
                        applications[index] = updatedApplication
                    }
                    updateApplicationInAdapter(updatedApplication)
                },
                onError = { error ->
                    Log.e("ApplyFragment", "Error fetching resume for application $applicationId: $error")
                }
            )
        }
    }

    private fun updateApplicationInAdapter(updatedApplication: ApplicationDTO) {
        val adapter = binding.recyclerApplications.adapter as? SeekerApplicationAdapter
        if (adapter != null) {
            val position = applications.indexOfFirst { it.applicationId == updatedApplication.applicationId }
            if (position != -1) {
                adapter.notifyItemChanged(position)
            }
        }
    }

    private fun showEmptyState() {
        binding.recyclerApplications.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
    }

    private fun updateUI() {
        if (applications.isEmpty()) {
            showEmptyState()
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerApplications.visibility = View.VISIBLE

            val seekerAdapter = SeekerApplicationAdapter(
                applications = applications,
                onViewDetails = { application ->
                    navigateToApplicationDetails(application)
                }
            )
            binding.recyclerApplications.adapter = seekerAdapter
        }
    }

    private fun navigateToApplicationDetails(application: ApplicationDTO) {
        val applicationId = application.applicationId
        if (applicationId != null) {
            val intent = Intent(requireContext(), ApplicationDetailsActivity::class.java).apply {
                putExtra("applicationId", applicationId)
            }
            startActivity(intent)
        } else {
            showToast("Không thể tìm thấy thông tin ứng tuyển")
        }
    }

    private fun getAuthToken(): String? {
        return sharedPrefs.authToken
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnFragmentInteractionListener {
        fun onNavigateToJobSearch()
    }
}