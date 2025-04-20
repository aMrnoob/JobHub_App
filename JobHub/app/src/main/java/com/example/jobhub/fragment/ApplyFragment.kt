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
import com.example.jobhub.dto.EmailRequestDTO
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.entity.enumm.ApplicationStatus
import com.example.jobhub.service.ApplicationService
import com.example.jobhub.service.EmailService
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

    private val emailService by lazy {
        RetrofitClient.createRetrofit().create(EmailService::class.java)
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

        binding.tvTitle.text = if (isEmployer) "Danh sách ứng viên" else "Công việc đã ứng tuyển"
    }

    private fun setupRecyclerView() {
        binding.recyclerApplications.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadApplications() {
        val token = getAuthToken() ?: return
        binding.progressBar.visibility = View.VISIBLE

        val apiCall = if (isEmployer) {
            // Get applications for employer's company
            applicationService.getApplicationsByCompanyId("Bearer $token", currentUser?.companyId ?: 0)
        } else {
            // Get seeker's applications
            applicationService.getApplicationsByUserId("Bearer $token", currentUser?.userId ?: 0)
        }

        ApiHelper().callApi(
            context = requireContext(),
            call = apiCall,
            onSuccess = { result ->
                binding.progressBar.visibility = View.GONE
                if (result != null) {
                    applications.clear()
                    applications.addAll(result)
                    updateUI()
                } else {
                    showToast("Không có dữ liệu ứng tuyển")
                    binding.tvNoApplications.visibility = View.VISIBLE
                    binding.recyclerApplications.visibility = View.GONE
                }
            },
            onError = { error ->
                binding.progressBar.visibility = View.GONE
                showToast("Không thể tải danh sách ứng tuyển: ${error ?: "Unknown error"}")
                binding.tvNoApplications.visibility = View.VISIBLE
                binding.recyclerApplications.visibility = View.GONE
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
                    onViewDetails = { application -> navigateToApplicationDetails(application) }
                )
                binding.recyclerApplications.adapter = employerAdapter
            } else {
                val seekerAdapter = SeekerApplicationAdapter(
                    applications = applications,
                    onViewDetails = { application -> navigateToApplicationDetails(application) }
                )
                binding.recyclerApplications.adapter = seekerAdapter
            }
        }
    }

    private fun navigateToApplicationDetails(application: ApplicationDTO) {
        // Implementation for navigation to application details
        // This would use the Navigation Component or Intent to navigate to details screen
        showToast("Xem chi tiết ${if (isEmployer) application.userDTO?.fullName else application.jobDTO?.title}")

        // Here you would implement the navigation logic:
        // val action = ApplyFragmentDirections.actionApplyFragmentToApplicationDetailsFragment(application.applicationId)
        // findNavController().navigate(action)
    }

    private fun handleAcceptApplication(application: ApplicationDTO) {
        if (!isEmployer) return

        val token = getAuthToken() ?: return
        val updatedApplication = application.copy(status = ApplicationStatus.ACCEPTED)

        ApiHelper().callApi(
            context = requireContext(),
            call = applicationService.updateApplicationStatus("Bearer $token", updatedApplication),
            onStart = { binding.progressBar.visibility = View.VISIBLE },
            onComplete = { binding.progressBar.visibility = View.GONE },
            onSuccess = { result ->
                if (result != null) {
                    // Update local data
                    val index = applications.indexOfFirst { it.applicationId == application.applicationId }
                    if (index != -1) {
                        applications[index] = result
                        binding.recyclerApplications.adapter?.notifyItemChanged(index)
                    }
                    showToast("Đã chấp nhận ứng viên")

                    // Send email notification to the seeker
                    sendApplicationStatusEmail(
                        application,
                        true,
                        "Chúc mừng! Đơn ứng tuyển của bạn đã được chấp nhận",
                        createAcceptanceEmailBody(application)
                    )
                }
            },
            onError = { error ->
                showToast("Không thể cập nhật trạng thái: ${error ?: "Unknown error"}")
            }
        )
    }

    private fun handleRejectApplication(application: ApplicationDTO) {
        if (!isEmployer) return

        val token = getAuthToken() ?: return
        val updatedApplication = application.copy(status = ApplicationStatus.REJECTED)

        ApiHelper().callApi(
            context = requireContext(),
            call = applicationService.updateApplicationStatus("Bearer $token", updatedApplication),
            onStart = { binding.progressBar.visibility = View.VISIBLE },
            onComplete = { binding.progressBar.visibility = View.GONE },
            onSuccess = { result ->
                if (result != null) {
                    // Update local data
                    val index = applications.indexOfFirst { it.applicationId == application.applicationId }
                    if (index != -1) {
                        applications[index] = result
                        binding.recyclerApplications.adapter?.notifyItemChanged(index)
                    }
                    showToast("Đã từ chối ứng viên")

                    // Send email notification to the seeker
                    sendApplicationStatusEmail(
                        application,
                        false,
                        "Thông báo về đơn ứng tuyển của bạn",
                        createRejectionEmailBody(application)
                    )
                }
            },
            onError = { error ->
                showToast("Không thể cập nhật trạng thái: ${error ?: "Unknown error"}")
            }
        )
    }

    private fun sendApplicationStatusEmail(
        application: ApplicationDTO,
        isAccepted: Boolean,
        subject: String,
        body: String
    ) {
        val token = getAuthToken() ?: return
        val recipientEmail = application.userDTO?.email ?: return

        val emailRequest = EmailRequestDTO(
            recipient = recipientEmail,
            subject = subject,
            body = body,
            isHtml = true
        )

        ApiHelper().callApi(
            context = requireContext(),
            call = emailService.sendEmail("Bearer $token", emailRequest),
            onSuccess = { success ->
                if (success == true) {
                    val status = if (isAccepted) "chấp nhận" else "từ chối"
                    showToast("Đã gửi email $status đến ứng viên")
                }
            },
            onError = { error ->
                showToast("Không thể gửi email: ${error ?: "Unknown error"}")
            }
        )
    }

    private fun createAcceptanceEmailBody(application: ApplicationDTO): String {
        val jobTitle = application.jobDTO?.title ?: "vị trí công việc"
        val companyName = application.jobDTO?.company?.companyName ?: "công ty"
        val userName = application.userDTO?.fullName ?: "Ứng viên"

        return """
            <html>
            <body>
                <h2>Xin chào $userName,</h2>
                <p>Chúc mừng! Đơn ứng tuyển của bạn cho vị trí <b>$jobTitle</b> tại <b>$companyName</b> đã được <span style="color: green; font-weight: bold;">CHẤP NHẬN</span>.</p>
                <p>Chúng tôi rất ấn tượng với hồ sơ của bạn và muốn mời bạn tiếp tục vào vòng phỏng vấn tiếp theo.</p>
                <p>Nhân viên tuyển dụng sẽ liên hệ với bạn trong thời gian sớm nhất để thảo luận về các bước tiếp theo.</p>
                <br>
                <p>Trân trọng,</p>
                <p>Đội ngũ tuyển dụng $companyName</p>
            </body>
            </html>
        """.trimIndent()
    }

    private fun createRejectionEmailBody(application: ApplicationDTO): String {
        val jobTitle = application.jobDTO?.title ?: "vị trí công việc"
        val companyName = application.jobDTO?.company?.companyName ?: "công ty"
        val userName = application.userDTO?.fullName ?: "Ứng viên"

        return """
            <html>
            <body>
                <h2>Xin chào $userName,</h2>
                <p>Cảm ơn bạn đã quan tâm và nộp đơn ứng tuyển cho vị trí <b>$jobTitle</b> tại <b>$companyName</b>.</p>
                <p>Sau quá trình xem xét kỹ lưỡng, chúng tôi rất tiếc phải thông báo rằng hồ sơ của bạn chưa phù hợp với yêu cầu của vị trí này tại thời điểm hiện tại.</p>
                <p>Chúng tôi khuyến khích bạn tiếp tục theo dõi các cơ hội việc làm khác tại công ty của chúng tôi trong tương lai.</p>
                <br>
                <p>Trân trọng,</p>
                <p>Đội ngũ tuyển dụng $companyName</p>
            </body>
            </html>
        """.trimIndent()
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