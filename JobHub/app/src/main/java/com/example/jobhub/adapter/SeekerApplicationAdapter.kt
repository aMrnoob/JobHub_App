package com.example.jobhub.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.databinding.ItemJobAppliedBinding
import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.entity.enumm.ApplicationStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val application = applications[position]
        val job = application.jobDTO
        val company = job.company

        holder.binding.tvJobTitle.text = job.title
        holder.binding.tvCompanyName.text = company.companyName

        Glide.with(holder.itemView.context)
            .load(company.logoUrl)
            .placeholder(R.drawable.ic_company_placeholder)
            .into(holder.binding.ivCompanyLogo)

        holder.binding.tvCvStatus.text = "Submitted CV"

        val applicationDate = formatLocalDateTime(application.applicationDate)
        holder.binding.tvApplicationDate.text = "Submition date: $applicationDate"

        if (application.coverLetter.isNotEmpty()) {
            holder.binding.tvCoverLetter.visibility = View.VISIBLE
            holder.binding.tvCoverLetter.text = "Cover letter: ${application.coverLetter}"
        } else {
            holder.binding.tvCoverLetter.visibility = View.GONE
        }

        val statusText = when (application.status) {
            ApplicationStatus.APPLIED -> "Submitted"
            ApplicationStatus.ACCEPTED -> "Accepted"
            ApplicationStatus.REJECTED -> "Rejected"
            ApplicationStatus.INTERVIEW -> "Interview"
        }
        holder.binding.tvStatus.text = statusText

        holder.binding.tvStatus.setBackgroundResource(
            when (application.status) {
                ApplicationStatus.APPLIED -> R.drawable.bg_status_applied
                ApplicationStatus.ACCEPTED -> R.drawable.bg_status_accepted
                ApplicationStatus.REJECTED -> R.drawable.bg_status_rejected
                ApplicationStatus.INTERVIEW -> R.drawable.bg_status_interview
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