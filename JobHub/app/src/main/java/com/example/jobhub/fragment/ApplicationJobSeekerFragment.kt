package com.example.jobhub.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.activity.VacancyActivity
import com.example.jobhub.adapter.JobAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainApplicationJobSeekerBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.service.JobService
import com.google.gson.Gson
import java.time.LocalDateTime

class ApplicationJobSeekerFragment : Fragment() {

    private var _binding: MainApplicationJobSeekerBinding? = null
    private val binding get() = _binding!!

    private val jobService: JobService by lazy {
        RetrofitClient.createRetrofit().create(JobService::class.java)
    }

    private val originalJobs: MutableList<ItemJobDTO> = mutableListOf()
    private val filteredJobs: MutableList<ItemJobDTO> = mutableListOf()

    private lateinit var jobAdapter: JobAdapter
    private var userDTO: UserDTO? = null
    private var currentFilter: String = "all"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainApplicationJobSeekerBinding.inflate(inflater, container, false)

        loadUserFromPrefs()
        setupRecyclerView()
        setupSearch()
        setupFilters()
        loadAppliedJobs()

        return binding.root
    }

    private fun loadUserFromPrefs() {
        val json = activity?.getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
            ?.getString("currentUser", null)

        if (!json.isNullOrEmpty()) {
            userDTO = Gson().fromJson(json, UserDTO::class.java)
        }
    }

    private fun getAuthToken(): String? {
        return activity?.getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
            ?.getString("authToken", null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(filteredJobs) { selectedJob ->
            val intent = Intent(requireContext(), VacancyActivity::class.java)
            val jobJson = Gson().toJson(selectedJob)
            requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
                .edit().putString("job", jobJson).apply()
            startActivity(intent)
        }

        binding.rvApplications.apply {
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadAppliedJobs() {
        val token = getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem ứng dụng của bạn", Toast.LENGTH_LONG).show()
            return
        }

        binding.tvName.text = "Đang tải dữ liệu..."

        ApiHelper().callApi(
            context = requireContext(),
            call = jobService.getAllJobsByUser("Bearer $token"),
            onSuccess = { response ->
                binding.tvName.text = "Ứng dụng của bạn"
                originalJobs.clear()
                response?.let { originalJobs.addAll(it) }


                filterJobs(currentFilter)
                jobAdapter.notifyDataSetChanged()
            },
        )
    }

    private fun setupSearch() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterJobs(currentFilter)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.ivSearch.setOnClickListener {
            hideKeyboard()
            filterJobs(currentFilter)
        }
    }

    private fun setupFilters() {
        binding.tvAllVacancies.setOnClickListener {
            currentFilter = "all"
            filterJobs(currentFilter)
        }

        binding.tvActive.setOnClickListener {
            currentFilter = "active"
            filterJobs(currentFilter)
        }

        binding.tvInactive.setOnClickListener {
            currentFilter = "expired"
            filterJobs(currentFilter)
        }
    }

    private fun filterJobs(status: String) {
        val query = binding.edtSearch.text.toString().trim().lowercase()

        val result = originalJobs.filter { job ->
            val isExpired = job.expirationDate.isBefore(LocalDateTime.now())

            val matchesStatus = when (status) {
                "active" -> !isExpired
                "expired" -> isExpired
                else -> true
            }

            val matchesSearch = job.title.lowercase().contains(query) ||
                    job.company.companyName.lowercase().contains(query)

            matchesStatus && matchesSearch
        }

        filteredJobs.clear()
        filteredJobs.addAll(result)
        jobAdapter.notifyDataSetChanged()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
