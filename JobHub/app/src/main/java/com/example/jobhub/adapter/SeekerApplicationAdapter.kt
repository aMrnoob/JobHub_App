package com.example.jobhub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.databinding.ItemApplicationBinding
import com.example.jobhub.databinding.ItemJobAppliedBinding
import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.entity.enumm.ApplicationStatus
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class SeekerApplicationAdapter(
    private val applications: List<ApplicationDTO>,
    private val onViewDetails: (ApplicationDTO) -> Unit
) : RecyclerView.Adapter<SeekerApplicationAdapter.ApplicationViewHolder>() {

    inner class ApplicationViewHolder(val binding: ItemJobAppliedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val binding = ItemJobAppliedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ApplicationViewHolder(binding)   
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val application = applications[position]
        val job = application.jobDTO
        val company = job?.company

        holder.binding.tvJobTitle.text = job?.title ?: "Không có tiêu đề"

        holder.binding.tvCompanyName.text = company?.companyName ?: "Công ty không xác định"

        Glide.with(holder.itemView.context)
            .load(company?.logoUrl)
            .placeholder(R.drawable.ic_company_placeholder)
            .into(holder.binding.ivCompanyLogo)

        holder.binding.tvCvStatus.text = if (application.resumeUrl != null)
            "Đã nộp CV"
        else
            "Chưa nộp CV"

        val applicationDate = formatLocalDateTime(application.applicationDate)
        holder.binding.tvApplicationDate.text = "Ngày nộp: $applicationDate"

        if (!application.coverLetter.isNullOrEmpty()) {
            holder.binding.tvCoverLetter.visibility = View.VISIBLE
            holder.binding.tvCoverLetter.text = "Thư xin việc: ${application.coverLetter}"
        } else {
            holder.binding.tvCoverLetter.visibility = View.GONE
        }

        val statusText = when (application.status) {
            ApplicationStatus.APPLIED -> "Đã nộp"
            ApplicationStatus.ACCEPTED -> "Đã chấp nhận"
            ApplicationStatus.REJECTED -> "Đã từ chối"
            ApplicationStatus.INTERVIEW -> "Phỏng vấn"
            else -> "Đã nộp"
        }
        holder.binding.tvStatus.text = statusText

        holder.binding.tvStatus.setBackgroundResource(
            when (application.status) {
                ApplicationStatus.APPLIED -> R.drawable.bg_status_applied
                ApplicationStatus.ACCEPTED -> R.drawable.bg_status_accepted
                ApplicationStatus.REJECTED -> R.drawable.bg_status_rejected
                ApplicationStatus.INTERVIEW -> R.drawable.bg_status_interview
                else -> R.drawable.bg_status_applied
            }
        )

        holder.itemView.setOnClickListener {
            onViewDetails(application)
        }
    }

    private fun formatLocalDateTime(dateTime: LocalDateTime?): String {
        if (dateTime == null) return "N/A"

        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
        return dateTime.format(formatter)
    }

    override fun getItemCount(): Int = applications.size
}