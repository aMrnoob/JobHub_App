package com.example.jobhub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ItemJobAppliedBinding
import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.entity.enumm.ApplicationStatus
import com.example.jobhub.service.ResumeService
import java.text.SimpleDateFormat
import java.util.Locale

class EmployerApplicationAdapter(
    private val applications: List<ApplicationDTO>,
    private val onAccept: (ApplicationDTO) -> Unit,
    private val onReject: (ApplicationDTO) -> Unit,
    private val onViewDetails: (ApplicationDTO) -> Unit
) : RecyclerView.Adapter<EmployerApplicationAdapter.ApplicationViewHolder>() {

    private val resumeService by lazy {
        RetrofitClient.createRetrofit().create(ResumeService::class.java)
    }

    inner class ApplicationViewHolder(val binding: ItemJobAppliedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val binding = ItemJobAppliedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ApplicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val application = applications[position]
        val user = application.userDTO
        val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val applyDate = application.applicationDate?.let { dateFormat.format(it) } ?: "N/A"

        holder.binding.tvApplicationDate.text = "Ngày nộp: $applyDate"
        holder.binding.tvCoverLetter.text = application.coverLetter ?: "Không có thư xin việc"

        // Set status text and color
        holder.binding.tvStatus.text = when (application.status) {
            ApplicationStatus.APPLIED -> "Đã nộp"
            ApplicationStatus.ACCEPTED -> "Đã chấp nhận"
            ApplicationStatus.REJECTED -> "Đã từ chối"
            ApplicationStatus.INTERVIEW -> "Đang xem"
        }

        holder.binding.tvStatus.setTextColor(
            when (application.status) {
                ApplicationStatus.APPLIED -> holder.itemView.context.getColor(R.color.status_applied)
                ApplicationStatus.ACCEPTED -> holder.itemView.context.getColor(R.color.status_accepted)
                ApplicationStatus.REJECTED -> holder.itemView.context.getColor(R.color.status_rejected)
                ApplicationStatus.INTERVIEW -> holder.itemView.context.getColor(R.color.status_interview)
            }
        )

        Glide.with(holder.itemView.context)
            .load(user?.imageUrl)
            .placeholder(R.drawable.simple_border)
            .into(holder.binding.ivCompanyLogo)

        val isPending = application.status == ApplicationStatus.APPLIED ||
                application.status == ApplicationStatus.INTERVIEW


        holder.itemView.setOnClickListener { onViewDetails(application) }
    }


    private fun getAuthToken(context: android.content.Context): String? {
        return context.getSharedPreferences("JobHubPrefs", android.content.Context.MODE_PRIVATE)
            .getString("authToken", null)
    }

    override fun getItemCount(): Int = applications.size
}