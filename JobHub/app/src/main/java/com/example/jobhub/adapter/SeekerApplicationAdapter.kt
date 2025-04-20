package com.example.jobhub.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.databinding.ItemJobAppliedBinding
import com.example.jobhub.dto.ApplicationDTO
import com.example.jobhub.entity.enumm.ApplicationStatus

class SeekerApplicationAdapter(
    private val applications: List<ApplicationDTO>,
    private val onAcceptClick: (ApplicationDTO) -> Unit,
    private val onRejectClick: (ApplicationDTO) -> Unit
) : RecyclerView.Adapter<SeekerApplicationAdapter.ApplicationViewHolder>() {

    inner class ApplicationViewHolder(val binding: ItemJobAppliedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val binding = ItemJobAppliedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ApplicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val application = applications[position]
        val user = application.userDTO

        Glide.with(holder.itemView.context)
            .load(user.imageUrl)
            .placeholder(R.drawable.error_image)
            .into(holder.binding.ivAvatar)

        holder.binding.tvUsername.text = user.fullName

        holder.binding.tvCoverLetter.text = application.coverLetter ?: "Không có thư xin việc"

        holder.binding.tvStatus.text = when (application.status) {
            ApplicationStatus.APPLIED -> "Đã nộp"
            ApplicationStatus.ACCEPTED -> "Đã chấp nhận"
            ApplicationStatus.REJECTED -> "Đã từ chối"
            ApplicationStatus.INTERVIEW -> "Đang xem"
        }

        holder.binding.tvStatus.setBackgroundResource(
            when (application.status) {
                ApplicationStatus.APPLIED -> R.drawable.bg_status_applied
                ApplicationStatus.ACCEPTED -> R.drawable.bg_status_accepted
                ApplicationStatus.REJECTED -> R.drawable.bg_status_rejected
                ApplicationStatus.INTERVIEW -> R.drawable.bg_status_interview
            }
        )

        // Xử lý click cho nút chấp nhận
        holder.binding.btnAccept.setOnClickListener {
            onAcceptClick(application)
        }

        // Xử lý click cho nút từ chối
        holder.binding.btnReject.setOnClickListener {
            onRejectClick(application)
        }
    }

    override fun getItemCount(): Int = applications.size
}
