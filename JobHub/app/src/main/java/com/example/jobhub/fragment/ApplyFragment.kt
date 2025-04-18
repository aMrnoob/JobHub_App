package com.example.jobhub.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.adapter.EmployerApplicationAdapter
import com.example.jobhub.adapter.SeekerApplicationAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainApplyBinding
import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.entity.enumm.ApplicationStatus
import com.example.jobhub.service.ApplicationService
import com.google.gson.Gson

class ApplyFragment : Fragment() {
    private var _binding: MainApplyBinding? = null
    private val binding get() = _binding!!

    private var isEmployer = false
    private var currentUser: UserDTO? = null
    private var applications = mutableListOf<ApplicationDTO>()

    private val applicationService by lazy {
        RetrofitClient.createRetrofit().create(ApplicationService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainApplyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()
        setupRecyclerView()
        loadApplications()
    }

    private fun loadUserData() {
        val prefs = requireActivity().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val userJson = prefs.getString("currentUser", null)
        val userRole = prefs.getString("userRole", null)

        currentUser = Gson().fromJson(userJson, UserDTO::class.java)
        isEmployer = userRole == "EMPLOYER"

        // Cập nhật tiêu đề
        binding.tvTitle.text = if (isEmployer) "Danh sách ứng viên" else "Công việc đã ứng tuyển"
    }

    private fun setupRecyclerView() {
        binding.recyclerApplications.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadApplications() {
        val token = getAuthToken() ?: return
        binding.progressBar.visibility = View.VISIBLE

        val apiCall = if (isEmployer) {
            applicationService.getApplicationsByEmployerId(token, currentUser?.userId?.toInt() ?: 0)
        } else {
            applicationService.getApplicationsByUserId(token, currentUser?.userId?.toInt() ?: 0)
        }

        ApiHelper().callApi(
            context = requireContext(),
            call = apiCall,
            onStart = { binding.progressBar.visibility = View.VISIBLE },
            onComplete = { binding.progressBar.visibility = View.GONE },
            onSuccess = { result ->
                if (result != null) {
                    applications.clear()
                    applications.addAll(result)
                    updateUI()
                } else {
                    showToast("Không có dữ liệu ứng tuyển")
                }
            },
            onError = {
                showToast("Không thể tải danh sách ứng tuyển")
            }
        )
    }

    private fun updateUI() {
        if (applications.isEmpty()) {
            binding.tvNoApplications.visibility = View.VISIBLE
            binding.recyclerApplications.visibility = View.GONE
        } else {
            binding.tvNoApplications.visibility = View.GONE
            binding.recyclerApplications.visibility = View.VISIBLE

            if (isEmployer) {
                val employerAdapter = EmployerApplicationAdapter(
                    applications = applications,
                    onAccept = { application -> handleAcceptApplication(application) },
                    onReject = { application -> handleRejectApplication(application) },
                    onViewDetails = { application ->
                        // Xử lý khi employer muốn xem chi tiết ứng viên
                        showToast("Xem chi tiết: ${application.userDTO?.fullName}")
                        // Có thể mở dialog hoặc sang activity tại đây
                    }
                )
                binding.recyclerApplications.adapter = employerAdapter
            } else {
                val seekerAdapter = SeekerApplicationAdapter(
                    applications = applications,
                    onAcceptClick = { /* Seeker không dùng */ },
                    onRejectClick = { /* Seeker không dùng */ }
                )
                binding.recyclerApplications.adapter = seekerAdapter
            }
        }
    }


    private fun handleAcceptApplication(application: ApplicationDTO) {
        if (!isEmployer) return

        val token = getAuthToken() ?: return
        val updatedApplication = application.copy(status = ApplicationStatus.ACCEPTED)

        ApiHelper().callApi(
            context = requireContext(),
            call = applicationService.updateApplicationStatus(token, updatedApplication),
            onStart = { binding.progressBar.visibility = View.VISIBLE },
            onComplete = { binding.progressBar.visibility = View.GONE },
            onSuccess = { result ->
                if (result != null) {
                    val index = applications.indexOfFirst { it.applicationId == application.applicationId }
                    if (index != -1) {
                        applications[index] = result
                        binding.recyclerApplications.adapter?.notifyItemChanged(index)
                    }
                    showToast("Đã chấp nhận ứng viên")
                }
            },
            onError = {
                showToast("Không thể cập nhật trạng thái")
            }
        )
    }

    private fun handleRejectApplication(application: ApplicationDTO) {
        if (!isEmployer) return

        val token = getAuthToken() ?: return
        val updatedApplication = application.copy(status = ApplicationStatus.REJECTED)

        ApiHelper().callApi(
            context = requireContext(),
            call = applicationService.updateApplicationStatus(token, updatedApplication),
            onStart = { binding.progressBar.visibility = View.VISIBLE },
            onComplete = { binding.progressBar.visibility = View.GONE },
            onSuccess = { result ->
                if (result != null) {
                    val index = applications.indexOfFirst { it.applicationId == application.applicationId }
                    if (index != -1) {
                        applications[index] = result
                        binding.recyclerApplications.adapter?.notifyItemChanged(index)
                    }
                    showToast("Đã từ chối ứng viên")
                }
            },
            onError = {
                showToast("Không thể cập nhật trạng thái")
            }
        )
    }

    private fun getAuthToken(): String? {
        return requireActivity().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
            .getString("authToken", null)
            ?.takeIf { it.isNotBlank() }
            ?: run {
                showToast("Vui lòng đăng nhập")
                null
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}