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
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.example.jobhub.entity.enumm.JobType
import com.example.jobhub.service.JobService
import java.time.LocalDateTime

class ApplicationEmployerFragment : Fragment() {

    private lateinit var jobAdapter: JobAdapter
    private lateinit var sharedPrefs: SharedPrefsManager

    private var _binding: MainApplicationEmployerBinding? = null
    private var allJobs: MutableList<ItemJobDTO> = mutableListOf()
    private var jobList: MutableList<ItemJobDTO> = mutableListOf()
    private var isFragmentVisible = false
    private var selectedExpertise: List<String>? = null
    private var selectedJobType: JobType? = null
    private var selectedSalaryCondition: ((String) -> Boolean)? = null
    private var selectedLocation: String? = null


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
        setupFilter()

        binding.ivMenu.setOnClickListener { showFilterLayout() }
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
                        job.isExpanded = !job.isExpanded
                        jobAdapter.notifyItemChanged(jobList.indexOf(job))
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
        val newView = layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(newView)
            .setCancelable(false)

        val btnYes = newView.findViewById<TextView>(R.id.btnYes)
        val btnNo = newView.findViewById<TextView>(R.id.btnNo)
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
    private fun setupFilter() {
        val filters = listOf(
            binding.tvAllVacancies to "ALL",
            binding.tvActive to "ACTIVE",
            binding.tvInactive to "EXPIRED"
        )

        filters.forEach { (textView, filterType) ->
            textView.setOnClickListener {
                filters.forEach { (tv, _) -> tv.isSelected = (tv == textView) }

                val now = LocalDateTime.now()
                val filtered = when (filterType) {
                    "ALL" -> allJobs
                    "ACTIVE" -> allJobs.filter { it.expirationDate.isAfter(now) || it.expirationDate.isEqual(now) }
                    "EXPIRED" -> allJobs.filter { it.expirationDate.isBefore(now) }
                    else -> allJobs
                }

                jobList.clear()
                jobList.addAll(filtered)
                jobAdapter.notifyDataSetChanged()

                binding.tvNoResults.visibility = if (jobList.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.tvAllVacancies.isSelected = true
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

        val filteredList = allJobs.filter { job -> job.title.contains(query, ignoreCase = true) || job.location.contains(query, ignoreCase = true) }.toMutableList()

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

    @SuppressLint("NotifyDataSetChanged", "InflateParams")
    private fun showFilterLayout() {
        val container = binding.root as ViewGroup
        val newView = layoutInflater.inflate(R.layout.dialog_menu_job, null)
        newView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        container.addView(newView)

        val btnApply = newView.findViewById<Button>(R.id.btnApply)
        btnApply.setOnClickListener {
            jobList.clear()
            jobList.addAll(applyFilter())
            jobAdapter.notifyDataSetChanged()
            container.removeView(newView)
            selectedExpertise = null
            selectedJobType = null
            selectedSalaryCondition = null
            selectedLocation = null
            binding.tvNoResults.visibility = if (jobList.isEmpty()) View.VISIBLE else View.GONE
        }

        val tvSoftwareDev = newView.findViewById<TextView>(R.id.tvSoftwareDev)
        val tvDataScience = newView.findViewById<TextView>(R.id.tvDataScience)
        val tvMachineLearning = newView.findViewById<TextView>(R.id.tvMachineLearning)
        val tvWebDevelopment = newView.findViewById<TextView>(R.id.tvWebDevelopment)
        val tvCybersecurity = newView.findViewById<TextView>(R.id.tvCybersecurity)
        val expertiseViews = listOf(tvSoftwareDev, tvDataScience, tvMachineLearning, tvWebDevelopment, tvCybersecurity)

        val tvFullTime = newView.findViewById<TextView>(R.id.tvFullTime)
        val tvPartTime = newView.findViewById<TextView>(R.id.tvPartTime)
        val tvInternship = newView.findViewById<TextView>(R.id.tvInternship)
        val tvContract = newView.findViewById<TextView>(R.id.tvContract)
        val jobTypeViews = listOf(tvFullTime, tvPartTime, tvInternship, tvContract)

        val tvSalary1 = newView.findViewById<TextView>(R.id.tvSalary1)
        val tvSalary2 = newView.findViewById<TextView>(R.id.tvSalary2)
        val tvSalary3 = newView.findViewById<TextView>(R.id.tvSalary3)
        val tvSalary4 = newView.findViewById<TextView>(R.id.tvSalary4)
        val salaryViews = listOf(tvSalary1, tvSalary2, tvSalary3, tvSalary4)

        val tvLocation1 = newView.findViewById<TextView>(R.id.tvLocation1)
        val tvLocation2 = newView.findViewById<TextView>(R.id.tvLocation2)
        val tvLocation3 = newView.findViewById<TextView>(R.id.tvLocation3)
        val tvLocation4 = newView.findViewById<TextView>(R.id.tvLocation4)
        val tvLocation5 = newView.findViewById<TextView>(R.id.tvLocation5)
        val tvLocation6 = newView.findViewById<TextView>(R.id.tvLocation6)
        val tvLocation7 = newView.findViewById<TextView>(R.id.tvLocation7)
        val locationViews = listOf(tvLocation1, tvLocation2, tvLocation3, tvLocation4, tvLocation5, tvLocation6, tvLocation7)

        fun highlightSelected(view: TextView, group: List<TextView>) {
            group.forEach { it.isSelected = false }
            view.isSelected = true
        }

        val expertiseMap = mapOf(
            tvSoftwareDev to listOf("Developer", "Software Engineer", "Programmer", "Backend Developer", "Frontend Developer", "Full-stack Developer", "C++ Developer", "Java Developer", "Python Developer"),
            tvDataScience to listOf("Data Scientist", "Data Analyst", "Big Data", "Data Visualization", "Data Modeling", "SQL", "Predictive Modeling", "Statistical Analysis", "Data Mining"),
            tvMachineLearning to listOf("Machine Learning Engineer", "ML Engineer", "AI Engineer", "Deep Learning", "Neural Networks", "Natural Language Processing", "Reinforcement Learning", "Computer Vision", "Algorithm Developer"),
            tvWebDevelopment to listOf("Web Developer", "Frontend Developer", "Backend Developer", "React Developer", "Angular Developer", "JavaScript Developer", "HTML", "CSS", "Node.js", "PHP Developer"),
            tvCybersecurity to listOf("Security Analyst", "Cybersecurity Engineer", "Ethical Hacker", "Penetration Testing", "Security Consultant", "Network Security", "Information Security", "Vulnerability Assessment", "Security Operations"),
        )
        expertiseMap.forEach { (textView, keywords) ->
            textView.setOnClickListener {
                highlightSelected(textView, expertiseViews)
                selectedExpertise = keywords
            }
        }

        val jobTypeMap = mapOf(
            tvFullTime to JobType.FULL_TIME,
            tvPartTime to JobType.PART_TIME,
            tvInternship to JobType.INTERNSHIP,
            tvContract to JobType.CONTRACT
        )
        jobTypeMap.forEach { (textView, type) ->
            textView.setOnClickListener {
                highlightSelected(textView, jobTypeViews)
                selectedJobType = type
            }
        }

        val salaryMap = mapOf<TextView, (String) -> Boolean>(
            tvSalary1 to { s -> extractSalary(s) < 1000 },
            tvSalary2 to { s -> extractSalary(s) < 2000 },
            tvSalary3 to { s -> extractSalary(s) < 5000 },
            tvSalary4 to { s -> extractSalary(s) >= 5000 }
        )
        salaryMap.forEach { (textView, condition) ->
            textView.setOnClickListener {
                highlightSelected(textView, salaryViews)
                selectedSalaryCondition = condition
            }
        }

        locationViews.forEach { textView ->
            textView.setOnClickListener {
                highlightSelected(textView, locationViews)
                selectedLocation = textView.text.toString()
            }
        }
    }

    private fun applyFilter(): List<ItemJobDTO> {
        return allJobs.filter { job ->
            val matchExpertise = selectedExpertise?.any { job.title.contains(it, ignoreCase = true) } ?: true
            val matchType = selectedJobType?.let { job.jobType == it } ?: true
            val matchSalary = selectedSalaryCondition?.invoke(job.salary) ?: true
            val matchLocation = selectedLocation?.let { job.location.contains(it, ignoreCase = true) } ?: true

            matchExpertise && matchType && matchSalary && matchLocation
        }
    }

    private fun extractSalary(salaryStr: String): Int {
        val salaryRange = salaryStr.split("-")
        val salary = salaryRange[0].filter { it.isDigit() }.toIntOrNull() ?: 0
        return salary
    }
}