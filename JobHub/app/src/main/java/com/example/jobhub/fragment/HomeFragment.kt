package com.example.jobhub.fragment

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.activity.VacancyActivity
import com.example.jobhub.adapter.JobAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainHomeBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.service.JobService
import com.google.gson.Gson

class HomeFragment : Fragment() {

    private var _binding: MainHomeBinding? = null
    private val binding get() = _binding!!

    private var selectedTextView: TextView? = null
    private val jobService: JobService by lazy {
        RetrofitClient.createRetrofit().create(JobService::class.java)
    }
    private var allJobs: MutableList<ItemJobDTO> = mutableListOf()
    private var jobList: MutableList<ItemJobDTO> = mutableListOf()
    private lateinit var jobAdapter: JobAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainHomeBinding.inflate(inflater, container, false)

        setupCategorySelection()
        setupAnimation()
        setupRecyclerView()
        setupSearchView()
        getAllJobs()

        return binding.root
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(jobList) { selectedJob ->
            val intent = Intent(requireContext(), VacancyActivity::class.java)
            val jobJson = Gson().toJson(selectedJob)
            val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("job", jobJson).apply()
            startActivity(intent)
        }

        binding.rvJob.apply {
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAllJobs() {
        val token = getAuthToken() ?: return

        ApiHelper().callApi(
            context = requireContext(),
            call = jobService.getAllJobsByUser("Bearer $token"),
            onSuccess = { response ->
                jobList.apply {
                    clear()
                    response?.let {
                        addAll(it)
                        allJobs.clear()
                        allJobs.addAll(it)
                    }
                }
                jobAdapter.notifyDataSetChanged()
            }
        )
    }

    private fun getAuthToken(): String? {
        return requireContext().getSharedPreferences("JobHubPrefs", AppCompatActivity.MODE_PRIVATE)
            .getString("authToken", null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun animateView(view: View) {
        ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f)
        ).apply {
            duration = 300
            start()
        }
    }

    private fun setupAnimation() {
        listOf(
            binding.ivNotification, binding.ivMenu,
            binding.tvTips, binding.tvViewMore1, binding.tvViewMore
        ).forEach { it.setOnClickListener { animateView(it) } }
    }

    private fun setupCategorySelection() {
        val categoryTextViews = listOf(
            binding.tvAllJob, binding.tvWriter, binding.tvDesign,
            binding.tvHR, binding.tvProgramer, binding.tvFinance,
            binding.tvCustomerService, binding.tvFoodRestaurant,
            binding.tvMusicProducer
        )

        updateSelectedCategory(binding.tvAllJob, categoryTextViews)

        categoryTextViews.forEach { textView ->
            textView.setOnClickListener {
                updateSelectedCategory(textView, categoryTextViews)
            }
        }
    }

    private fun updateSelectedCategory(selected: TextView, categoryTextViews: List<TextView>) {
        selected.isSelected = true

        categoryTextViews.filter { it != selected }.forEach {
            it.isSelected = false
        }

        selectedTextView = selected
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterCompanies(newText.orEmpty())
                return true
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterCompanies(query: String) {
        if (query.isEmpty()) {
            jobList.clear()
            jobList.addAll(allJobs)
            jobAdapter.notifyDataSetChanged()
            binding.tvNoResults.visibility = View.GONE
            return
        }

        val filteredList = allJobs.filter { job ->
            job.title.contains(query, ignoreCase = true) || job.location.contains(query, ignoreCase = true)
        }.toMutableList()

        jobList.clear()
        jobList.addAll(filteredList)
        jobAdapter.notifyDataSetChanged()

        binding.tvNoResults.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }
}