package com.example.jobhub.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jobhub.R
import com.example.jobhub.activity.JobActivity
import com.example.jobhub.activity.VacancyActivity
import com.example.jobhub.adapter.JobAdapter
import com.example.jobhub.anim.AnimationHelper
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.MainApplicationEmployerBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.entity.enumm.JobAction
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.service.JobService
import java.time.LocalDateTime

class ApplicationEmployerFragment : Fragment() {

    private lateinit var jobAdapter: JobAdapter
    private lateinit var sharedPrefs: SharedPrefsManager

    private var _binding: MainApplicationEmployerBinding? = null
    private var allJobs: MutableList<ItemJobDTO> = mutableListOf()
    private var jobList: MutableList<ItemJobDTO> = mutableListOf()
    private var isFragmentVisible = false

    private val binding get() = _binding!!
    private val jobService: JobService by lazy { RetrofitClient.createRetrofit().create(JobService::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainApplicationEmployerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = SharedPrefsManager(requireContext())
        refreshHandler.post(refreshRunnable)

        setupRecyclerView()
        getAllJobs()
        setupSearchView()

        binding.ivAddCompany.setOnClickListener {
            AnimationHelper.animateScale(it)
            val intent = Intent(requireContext(), JobActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(
            jobList,
            onActionClick = { job, action ->
                when (action) {
                    JobAction.CLICK -> {

                    }
                    JobAction.EDIT -> {
                        val intent = Intent(requireContext(), VacancyActivity::class.java)
                        sharedPrefs.saveCurrentJob(job)
                        startActivity(intent)
                    }
                    JobAction.DELETE -> { deleteJob(job.jobId) }
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
    private fun getAllJobs() {
        val token = sharedPrefs.authToken ?: return

        ApiHelper().callApi(
            context = requireContext(),
            call = jobService.getAllJobsByUser("Bearer $token"),
            onSuccess = { response ->
                allJobs.clear()
                response?.let { allJobs.addAll(it) }
                if (binding.searchView.query.isNullOrEmpty()) {
                    jobList.clear()
                    jobList.addAll(allJobs)
                }
                jobAdapter.notifyDataSetChanged()
            }
        )
    }

    private fun deleteJob(jobId: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)

        val btnYes = dialogView.findViewById<TextView>(R.id.btnYes)
        val btnNo = dialogView.findViewById<TextView>(R.id.btnNo)
        val alertDialog = dialogBuilder.create()

        btnYes.setOnClickListener {
            ApiHelper().callApi(
                context = requireContext(),
                call = jobService.deleteJob(jobId),
                onSuccess = {
                    alertDialog.dismiss()
                    getAllJobs()
                }
            )
        }

        btnNo.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
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

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (isFragmentVisible && binding.searchView.query.isNullOrEmpty()) {
                getAllJobs()
            }
            refreshHandler.postDelayed(this, 60000)
        }
    }

    override fun onResume() {
        super.onResume()
        isFragmentVisible = true
        refreshHandler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        isFragmentVisible = false
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}