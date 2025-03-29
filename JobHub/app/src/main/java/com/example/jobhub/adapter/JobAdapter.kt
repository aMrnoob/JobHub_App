package com.example.jobhub.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.databinding.ItemJobBinding
import com.example.jobhub.dto.employer.JobInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class JobAdapter(private val jobList: List<JobInfo>, private val onItemClick: (JobInfo) -> Unit) :
    RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    inner class JobViewHolder(private val binding: ItemJobBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(job: JobInfo) {

            Glide.with(binding.root.context)
                .load(job.companyInfo?.logoUrl)
                .placeholder(R.drawable.error_image)
                .error(R.drawable.error_image)
                .into(binding.ivImgJob)

            binding.tvTitle.text = job.title
            binding.tvLocationJobType.text = "${job.location} - ${job.jobType}"
            binding.tvSalary.text = job.salary

            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")

            val expirationDateFormatted = formatDateString(job.expirationDate, inputFormatter, outputFormatter)

            binding.tvPostExpirationDate.text = "Exp: $expirationDateFormatted"

            val currentDate = LocalDateTime.now()
            val expirationDate = try {
                job.expirationDate?.let { LocalDateTime.parse(it, inputFormatter) }
            } catch (e: DateTimeParseException) {
                null
            }

            if (expirationDate != null && expirationDate.isBefore(currentDate)) {
                binding.tvStatus.text = "Expired"
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.tvStatus.context, R.color.red_700))
                binding.tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.tvStatus.context, R.color.red_300))
            } else {
                binding.tvStatus.text = "Active"
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.tvStatus.context, R.color.green_500))
                binding.tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.tvStatus.context, R.color.green_200))
            }

            binding.root.setOnClickListener {
                onItemClick(job)
            }
        }

        private fun formatDateString(dateString: String?, inputFormatter: DateTimeFormatter, outputFormatter: DateTimeFormatter): String {
            return try {
                dateString?.let {
                    LocalDateTime.parse(it, inputFormatter).toLocalDate().format(outputFormatter)
                } ?: "N/A"
            } catch (e: DateTimeParseException) {
                "N/A"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding =
            ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobList[position])
    }

    override fun getItemCount(): Int = jobList.size
}