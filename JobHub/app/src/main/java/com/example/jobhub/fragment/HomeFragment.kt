package com.example.jobhub.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jobhub.R
import com.example.jobhub.activity.ApplicantActivity
import com.example.jobhub.activity.ApplicationDetailsActivity
import com.example.jobhub.activity.ApplyJobActivity
import com.example.jobhub.activity.InforJobActivity
import com.example.jobhub.activity.VacancyActivity
import com.example.jobhub.adapter.JobAdapter
import com.example.jobhub.adapter.NotificationAdapter
import com.example.jobhub.anim.AnimationHelper
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.MainHomeBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.dto.MarkAsReadDTO
import com.example.jobhub.dto.NotificationEntityDTO
import com.example.jobhub.entity.enumm.JobAction
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.service.ApplicationService
import com.example.jobhub.service.JobService
import com.example.jobhub.service.NotificationService

class HomeFragment : Fragment() {
    private lateinit var jobAdapter: JobAdapter
    private lateinit var sharedPrefs: SharedPrefsManager

    private var _binding: MainHomeBinding? = null
    private var selectedTextView: TextView? = null

    private var selectedNotification: NotificationEntityDTO? = null
    private var allJobs: MutableList<ItemJobDTO> = mutableListOf()
    private var listNotification: List<NotificationEntityDTO> = emptyList()
    private var jobList: MutableList<ItemJobDTO> = mutableListOf()
    private var isFragmentVisible = false

    private val binding get() = _binding!!
    private val jobService: JobService by lazy { RetrofitClient.createRetrofit().create(JobService::class.java) }
    private val notificationService: NotificationService by lazy { RetrofitClient.createRetrofit().create(NotificationService::class.java) }
    private val applicationService: ApplicationService by lazy { RetrofitClient.createRetrofit().create(ApplicationService::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = SharedPrefsManager(requireContext())
        refreshHandler.post(refreshRunnable)

        setupAnimation()
        setupRecyclerView()
        getAllJobs()
        setupCategorySelection()
        setupSearchView()
        setupUIElements()

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

        categoryMap.forEach { (textView, category) -> textView.tag = category }
    }

    private fun setupUIElements() {
        binding.ivNotification.setOnClickListener {
            AnimationHelper.animateScale(it)
            showNotificationsDialog()
        }

        binding.tvViewMore.setOnClickListener {
            val url = "http://192.168.83.233:8080/api/admin/tips"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        binding.btnReadMore.setOnClickListener {
            val url = "http://192.168.83.233:8080/api/admin/tips"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        val currentRole = sharedPrefs.role

        jobAdapter = JobAdapter(
            jobList,
            onActionClick = { job, action ->
                when (action) {
                    JobAction.CLICK -> {
                        if (currentRole == Role.JOB_SEEKER) {
                            val intent = Intent(requireContext(), InforJobActivity::class.java)
                            sharedPrefs.saveCurrentJob(job)
                            startActivity(intent)
                        }
                    }

                    JobAction.BOOKMARK -> {

                    }

                    JobAction.APPLY -> {
                        val intent = Intent(requireContext(), ApplyJobActivity::class.java)
                        sharedPrefs.saveCurrentJob(job)
                        startActivity(intent)
                    }

                    JobAction.EDIT -> {
                        val intent = Intent(requireContext(), VacancyActivity::class.java)
                        sharedPrefs.saveCurrentJob(job)
                        startActivity(intent)
                    }

                    JobAction.DELETE -> { deleteJob(job.jobId) }
                }
            }
        )

        binding.rvJob.apply {
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun getAllJobs() {
        val token = sharedPrefs.authToken ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.rvJob.visibility = View.GONE
        binding.tvNoResults.visibility = View.GONE

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

                binding.progressBar.visibility = View.GONE
                binding.rvJob.visibility = View.VISIBLE

                if (jobList.isEmpty()) {
                    binding.tvNoResults.visibility = View.VISIBLE
                } else {
                    binding.tvNoResults.visibility = View.GONE
                }

                jobAdapter.notifyDataSetChanged()
            },
            onError = {
                binding.progressBar.visibility = View.GONE
                binding.rvJob.visibility = View.VISIBLE

                if (jobList.isEmpty()) {
                    binding.tvNoResults.visibility = View.VISIBLE
                    binding.tvNoResults.text = "Có lỗi xảy ra khi tải dữ liệu"
                }
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
                onSuccess = { alertDialog.dismiss() }
            )
        }

        btnNo.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun setupAnimation() {
        listOf(
            binding.ivMenu, binding.tvTips, binding.tvViewMore1, binding.tvViewMore
        ).forEach { it.setOnClickListener { AnimationHelper.animateScale(it) } }
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

        binding.progressBar.visibility = View.VISIBLE
        binding.rvJob.visibility = View.GONE
        binding.tvNoResults.visibility = View.GONE

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

        binding.progressBar.visibility = View.GONE
        binding.rvJob.visibility = View.VISIBLE
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

        binding.progressBar.visibility = View.VISIBLE
        binding.rvJob.visibility = View.GONE
        binding.tvNoResults.visibility = View.GONE

        if (query.isEmpty()) {
            jobList.clear()
            jobList.addAll(allJobs)
            jobAdapter.notifyDataSetChanged()
            binding.tvNoResults.visibility = View.GONE
        } else {
            val filteredList = allJobs.filter { job ->
                job.title.contains(query, ignoreCase = true) || job.location.contains(query, ignoreCase = true)
            }.toMutableList()

            jobList.clear()
            jobList.addAll(filteredList)
            jobAdapter.notifyDataSetChanged()

            binding.tvNoResults.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.progressBar.visibility = View.GONE
        binding.rvJob.visibility = View.VISIBLE
    }

    private val categoryKeywords: Map<String, List<String>> = mapOf(
        "Software Development" to listOf("Developer", "Software", "Programmer", "Backend", "Frontend", "Full-stack", "C++", "Java", "Python"),
        "Data Science" to listOf("Data Scientist", "Data Analyst", "Big Data", "Data Visualization", "Data Modeling", "SQL", "Predictive Modeling", "Statistical Analysis", "Data Mining"),
        "Machine Learning" to listOf("Machine Learning Engineer", "ML Engineer", "AI Engineer", "Deep Learning", "Neural Networks", "Natural Language Processing", "Reinforcement Learning", "Computer Vision", "Algorithm Developer"),
        "Web Development" to listOf("Web Developer", "Frontend Developer", "Backend Developer", "React Developer", "Angular Developer", "JavaScript Developer", "HTML", "CSS", "Node.js", "PHP Developer"),
        "Cloud Computing" to listOf("Cloud Engineer", "AWS", "Azure", "Google Cloud", "Cloud Architect", "Cloud Solutions Engineer", "DevOps", "Kubernetes", "Cloud Infrastructure"),
        "Network Engineering" to listOf("Network Engineer", "Network Administrator", "Network Architect", "Routing", "Switching", "Cisco", "Network Security", "VPN", "TCP/IP", "LAN", "WAN"),
        "Cybersecurity" to listOf("Security Analyst", "Cybersecurity Engineer", "Ethical Hacker", "Penetration Testing", "Security Consultant", "Network Security", "Information Security", "Vulnerability Assessment", "Security Operations"),
        "DevOps Engineering" to listOf("DevOps Engineer", "Continuous Integration", "Continuous Deployment", "Jenkins", "Docker", "Kubernetes", "Automation", "Infrastructure as Code", "CI/CD")
    )

    @SuppressLint("SetTextI18n")
    private fun showNotificationsDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_notifications)

        val window = dialog.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.TOP)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.rvNotifications)
        val emptyView = dialog.findViewById<TextView>(R.id.tvEmptyNotifications)
        val btnViewAll = dialog.findViewById<Button>(R.id.btnViewAll)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (listNotification.isEmpty()) {
            emptyView.text = "Đang tải thông báo..."
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE

            val token = sharedPrefs.authToken ?: return
            ApiHelper().callApi(
                context = requireContext(),
                call = notificationService.getAllNotifications("Bearer $token"),
                onSuccess = { response ->
                    if (!response.isNullOrEmpty()) {
                        listNotification = response

                        recyclerView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE
                        setupNotificationAdapter(recyclerView, listNotification, dialog)
                    } else {
                        btnViewAll.visibility = View.GONE
                        emptyView.text = "Không có thông báo nào."
                    }
                }
            )
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            btnViewAll.visibility = View.VISIBLE
            setupNotificationAdapter(recyclerView, listNotification, dialog)
        }

        dialog.show()
    }

    private fun setupNotificationAdapter(
        recyclerView: RecyclerView,
        notifications: List<NotificationEntityDTO>,
        dialog: Dialog
    ) {
        val recentNotifications = notifications.take(5)
        val adapter = NotificationAdapter(recentNotifications,
            object : NotificationAdapter.OnNotificationListener {
                override fun onItemClick(notification: NotificationEntityDTO) {
                    selectedNotification = notification
                    markNotificationAsRead(notification.id)
                    dialog.dismiss()
                }

                override fun onViewDetailClick(notification: NotificationEntityDTO) {
                    selectedNotification = notification
                    navigateToNotificationDetails()
                    dialog.dismiss()
                }
            })
        recyclerView.adapter = adapter
    }

    private fun navigateToNotificationDetails() {
        val applicationId = selectedNotification?.application?.applicationId
        if (applicationId != null) {
            markNotificationAsRead(selectedNotification?.id)

            val currentRole = sharedPrefs.role
            if (currentRole == Role.EMPLOYER) {
                getApplication(applicationId)
                startActivity(Intent(requireContext(), ApplicantActivity::class.java))
            } else {
                val intent = Intent(requireContext(), ApplicationDetailsActivity::class.java).apply { putExtra("applicationId", applicationId) }
                startActivity(intent)
            }
        }
    }

    private fun markNotificationAsRead(notificationId: Long?) {
        notificationId?.let {
            val token = sharedPrefs.authToken ?: return

            ApiHelper().callApi(
                context = requireContext(),
                call = notificationService.markAsRead(markAsReadDTO = MarkAsReadDTO("Bearer $token", notificationId)),
                onSuccess = { }
            )
        }
    }

    private fun getApplication(applicationId: Int) {
        val token = sharedPrefs.authToken ?: return
        binding.progressBar.visibility = View.VISIBLE

        ApiHelper().callApi(
            context = requireContext(),
            call = applicationService.getApplicationById("Bearer $token", applicationId),
            onSuccess = { application ->
                binding.progressBar.visibility = View.GONE
                if (application != null) { sharedPrefs.saveCurrentApplication(application) }
            }
        )
    }

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (isFragmentVisible && binding.searchView.query.isNullOrEmpty()) { getAllJobs() }
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
}