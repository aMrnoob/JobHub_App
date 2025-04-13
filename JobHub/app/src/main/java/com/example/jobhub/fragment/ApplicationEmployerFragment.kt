package com.example.jobhub.fragment

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.activity.JobActivity
import com.example.jobhub.activity.VacancyActivity
import com.example.jobhub.adapter.JobAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.MainApplicationEmployerBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.entity.enumm.ActionType
import com.example.jobhub.service.JobService

class ApplicationEmployerFragment : Fragment() {

    private lateinit var jobAdapter: JobAdapter
    private lateinit var sharedPrefs: SharedPrefsManager

    private var _binding: MainApplicationEmployerBinding? = null
    private var allJobs: MutableList<ItemJobDTO> = mutableListOf()
    private var jobList: MutableList<ItemJobDTO> = mutableListOf()

    private val binding get() = _binding!!
    private val jobService: JobService by lazy { RetrofitClient.createRetrofit().create(JobService::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainApplicationEmployerBinding.inflate(inflater, container, false)
        sharedPrefs = SharedPrefsManager(requireContext())

        setupRecyclerView()
        getAllJobs()
        setupSearchView()

        binding.ivAddCompany.setOnClickListener {
            animateView(it)
            val intent = Intent(requireContext(), JobActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(
            jobList,
            onActionClick = { selectedJob, action ->
                when (action) {
                    ActionType.CLICK -> {

                    }

                    ActionType.EDIT -> {
                        val intent = Intent(requireContext(), VacancyActivity::class.java)
                        sharedPrefs.saveCurrentJob(selectedJob)
                        startActivity(intent)
                    }

                    ActionType.DELETE -> {

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
    private fun getAllJobs() {
        val token = sharedPrefs.authToken ?: return

        ApiHelper().callApi(
            context = requireContext(),
            call = jobService.getAllJobsByUser("Bearer $token"),
            onSuccess = { response ->
                allJobs.clear()
                response?.let { allJobs.addAll(it) }

                jobList.clear()
                jobList.addAll(allJobs)

                jobAdapter.notifyDataSetChanged()
            }
        )
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