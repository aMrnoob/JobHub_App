package com.example.jobhub.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.jobhub.R
import com.example.jobhub.databinding.ItemEntityJobBinding
import com.example.jobhub.entity.Job
import com.example.jobhub.entity.enumm.JobType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JobEntityAdapter(
    private val jobList: List<Job>,
) : RecyclerView.Adapter<JobEntityAdapter.JobViewHolder>() {

    inner class JobViewHolder(private val binding: ItemEntityJobBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(job: Job) {
            with(binding) {
                tvTitle.text = job.title
                tvLocationJobType.text = "${job.location} - ${job.jobType?.let { formatJobType(it) }}"
                tvSalary.text = job.salary

                val formattedDate = job.expirationDate?.format(DateTimeFormatter.ofPattern("d/M/yyyy"))
                tvPostExpirationDate.text = "Exp: $formattedDate"

                val isExpired = isExpired(job.expirationDate)
                tvStatus.text = if (isExpired) "Expired" else "Active"
                val (textColor, bgColor) = if (isExpired) {
                    R.color.red_700 to R.color.red_300
                } else {
                    R.color.green_500 to R.color.green_200
                }

                tvStatus.setTextColor(ContextCompat.getColor(root.context, textColor))
                tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(root.context, bgColor))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemEntityJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobList[position])
    }

    override fun getItemCount(): Int = jobList.size

    private fun formatJobType(jobType: JobType): String {
        return jobType.name.split("_")
            .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
    }

    fun isExpired(expirationDate: String?): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            val date = LocalDateTime.parse(expirationDate, formatter)
            date.isBefore(LocalDateTime.now())
        } catch (e: Exception) {
            false
        }
    }
}
