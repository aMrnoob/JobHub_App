package com.example.jobhub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.databinding.ItemJobAppliedBinding
import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.entity.enumm.ApplicationStatus
import java.text.SimpleDateFormat
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
        val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val applyDate = application.applicationDate?.let { dateFormat.format(it) } ?: "N/A"

        // Show job information for seeker's view
        holder.binding.tvUsername.text = job?.title ?: "Không có tiêu đề"
        holder.binding.tvApplicationDate.text = "Ngày nộp: $applyDate"
        holder.binding.tvCoverLetter.text = application.coverLetter ?: "Không có thư xin việc"
        holder.binding.tvCvAttached.text = "Tình trạng: Đã nộp CV"

        // Set status text and color
        holder.binding.tvStatus.text = when (application.status) {
            ApplicationStatus.APPLIED -> "Đã nộp"
            ApplicationStatus.ACCEPTED -> "Đã chấp nhận"
            ApplicationStatus.REJECTED -> "Đã từ chối"
            ApplicationStatus.REVIEWED -> "Đang xem"
        }

        holder.binding.tvStatus.setTextColor(
            when (application.status) {
                ApplicationStatus.APPLIED -> holder.itemView.context.getColor(R.color.status_applied)
                ApplicationStatus.ACCEPTED -> holder.itemView.context.getColor(R.color.status_accepted)
                ApplicationStatus.REJECTED -> holder.itemView.context.getColor(R.color.status_rejected)
                ApplicationStatus.REVIEWED -> holder.itemView.context.getColor(R.color.status_reviewed)
            }
        )

        Glide.with(holder.itemView.context)
            .load(job?.company?.logoUrl ?: "")
            .placeholder(R.drawable.simple_border)
            .into(holder.binding.ivAvatar)

        // Hide employer buttons for seeker view
        holder.binding.layoutEmployerButtons.visibility = View.GONE

        // Set item click listener for viewing details
        holder.itemView.setOnClickListener { onViewDetails(application) }
    }

    override fun getItemCount(): Int = applications.size
}