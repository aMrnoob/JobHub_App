package com.example.jobhub.fragment

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import com.example.jobhub.activity.VacancyActivity
import com.example.jobhub.adapter.JobAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.MainHomeBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.entity.enumm.ActionType
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.service.JobService
import java.time.LocalDateTime

class HomeFragment : Fragment() {
    private lateinit var jobAdapter: JobAdapter
    private lateinit var sharedPrefs: SharedPrefsManager

    private var _binding: MainHomeBinding? = null
    private var selectedTextView: TextView? = null
    private var allJobs: MutableList<ItemJobDTO> = mutableListOf()
    private var jobList: MutableList<ItemJobDTO> = mutableListOf()

    private val binding get() = _binding!!
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val jobService: JobService by lazy { RetrofitClient.createRetrofit().create(JobService::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainHomeBinding.inflate(inflater, container, false)
        sharedPrefs = SharedPrefsManager(requireContext())

        setupAnimation()
        setupRecyclerView()
        getAllJobs()
        setupCategorySelection()
        setupSearchView()

        val categoryMap = mapOf(
            binding.tvAllJob to "All Job",
            binding.tvSoftwareDev to "Software Development",
            binding.tvDataScience to "Data Science",
            binding.tvMachineLearning to "Machine Learning",
            binding.tvWebDevelopment to "Web Development",
            binding.tvCloudComputing to "Cloud Computing",
            binding.tvNetworkEngineer to "Network Engineering",
            binding.tvCybersecurity to "Cybersecurity",
            binding.tvDevOpsEngineer to "DevOps Engineering"
        )

        categoryMap.forEach { (textView, category) ->
            textView.tag = category
        }

        binding.tvViewMore.setOnClickListener {
            val url = "http://192.168.1.15:8080/api/admin/tips"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        binding.btnReadMore.setOnClickListener {
            val url = "http://192.168.1.15:8080/api/admin/tips"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        val currentRole = sharedPrefs.role

        jobAdapter = JobAdapter(
            jobList,
            onActionClick = { selectedJob, action ->
                when (action) {
                    ActionType.CLICK -> {
                        if (currentRole == Role.JOB_SEEKER) {
                            val intent = Intent(requireContext(), VacancyActivity::class.java)
                            sharedPrefs.saveCurrentJob(selectedJob)
                            startActivity(intent)
                        }
                    }

                    ActionType.BOOKMARK -> {

                    }

                    ActionType.APPLY -> {

                    }

                    ActionType.EDIT -> {
                        val intent = Intent(requireContext(), VacancyActivity::class.java)
                        sharedPrefs.saveCurrentJob(selectedJob)
                        startActivity(intent)
                    }

                    ActionType.DELETE -> {

                    }
                }
            }
        )

        binding.rvJob.apply {
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAllJobs() {
        val token = sharedPrefs.authToken ?: return
        val currentRole = sharedPrefs.role

        ApiHelper().callApi(
            context = requireContext(),
            call = jobService.getAllJobsByUser("Bearer $token"),
            onSuccess = { response ->
                val jobs = response?.let {
                    if (currentRole == Role.JOB_SEEKER) {
                        it.filter { job -> job.expirationDate.isAfter(LocalDateTime.now()) }
                    } else {
                        it
                    }
                } ?: emptyList()

                allJobs.clear()
                allJobs.addAll(jobs)
                if (jobList.isEmpty()) {
                    jobList.clear()
                    jobList.addAll(allJobs)
                }
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

    private fun setupAnimation() {
        listOf(
            binding.ivNotification, binding.ivMenu,
            binding.tvTips, binding.tvViewMore1, binding.tvViewMore
        ).forEach { it.setOnClickListener { animateView(it) } }
    }

    private fun setupCategorySelection() {
        val categoryTextViews = listOf(
            binding.tvAllJob, binding.tvSoftwareDev, binding.tvDataScience,
            binding.tvMachineLearning, binding.tvWebDevelopment, binding.tvCloudComputing,
            binding.tvNetworkEngineer, binding.tvCybersecurity,
            binding.tvDevOpsEngineer
        )

        updateSelectedCategory(binding.tvAllJob, categoryTextViews)

        categoryTextViews.forEach { textView ->
            textView.setOnClickListener {
                updateSelectedCategory(textView, categoryTextViews)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateSelectedCategory(selected: TextView, categoryTextViews: List<TextView>) {
        selected.isSelected = true
        categoryTextViews.filter { it != selected }.forEach { it.isSelected = false }
        selectedTextView = selected

        val category = selected.tag as? String

        if (category == "All Job" || category == null) {
            jobList.clear()
            jobList.addAll(allJobs)
            jobAdapter.notifyDataSetChanged()
            binding.tvNoResults.visibility = View.GONE
        } else if (categoryKeywords.containsKey(category)) {
            val keywords = categoryKeywords[category] ?: emptyList()
            val filtered = allJobs.filter { job ->
                keywords.any { keyword ->
                    job.title.contains(keyword, ignoreCase = true)
                }
            }

            jobList.clear()
            jobList.addAll(filtered)
            jobAdapter.notifyDataSetChanged()
            binding.tvNoResults.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
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

    private val categoryKeywords: Map<String, List<String>> = mapOf(
        "Software Development" to listOf("Developer", "Software Engineer", "Programmer", "Backend Developer", "Frontend Developer", "Full-stack Developer", "C++ Developer", "Java Developer", "Python Developer"),
        "Data Science" to listOf("Data Scientist", "Data Analyst", "Big Data", "Data Visualization", "Data Modeling", "SQL", "Predictive Modeling", "Statistical Analysis", "Data Mining"),
        "Machine Learning" to listOf("Machine Learning Engineer", "ML Engineer", "AI Engineer", "Deep Learning", "Neural Networks", "Natural Language Processing", "Reinforcement Learning", "Computer Vision", "Algorithm Developer"),
        "Web Development" to listOf("Web Developer", "Frontend Developer", "Backend Developer", "React Developer", "Angular Developer", "JavaScript Developer", "HTML", "CSS", "Node.js", "PHP Developer"),
        "Cloud Computing" to listOf("Cloud Engineer", "AWS", "Azure", "Google Cloud", "Cloud Architect", "Cloud Solutions Engineer", "DevOps", "Kubernetes", "Cloud Infrastructure"),
        "Network Engineering" to listOf("Network Engineer", "Network Administrator", "Network Architect", "Routing", "Switching", "Cisco", "Network Security", "VPN", "TCP/IP", "LAN", "WAN"),
        "Cybersecurity" to listOf("Security Analyst", "Cybersecurity Engineer", "Ethical Hacker", "Penetration Testing", "Security Consultant", "Network Security", "Information Security", "Vulnerability Assessment", "Security Operations"),
        "DevOps Engineering" to listOf("DevOps Engineer", "Continuous Integration", "Continuous Deployment", "Jenkins", "Docker", "Kubernetes", "Automation", "Infrastructure as Code", "CI/CD")
    )

    private val refreshRunnable = object : Runnable {
        override fun run() {
            getAllJobs()
            refreshHandler.postDelayed(this, 3000)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHandler.postDelayed(refreshRunnable, 3000)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
    }
}