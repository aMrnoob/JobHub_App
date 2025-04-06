package com.example.jobhub.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.databinding.ItemJobBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.entity.enumm.JobType
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.entity.enumm.Role
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JobAdapter(private val jobList: List<ItemJobDTO>, private val onItemClick: (ItemJobDTO) -> Unit) :
    RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    inner class JobViewHolder(private val binding: ItemJobBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(itemJobDTO: ItemJobDTO) {

            Glide.with(binding.root.context)
                .load(itemJobDTO.company.logoUrl)
                .placeholder(R.drawable.error_image)
                .error(R.drawable.error_image)
                .into(binding.ivImgJob)

            binding.tvTitle.text = itemJobDTO.title
            binding.tvLocationJobType.text = "${itemJobDTO.location} - ${formatJobType(itemJobDTO.jobType)}"
            binding.tvSalary.text = itemJobDTO.salary

            val outputFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")

            val expirationDateFormatted = itemJobDTO.expirationDate.format(outputFormatter) ?: "N/A"

            binding.tvPostExpirationDate.text = "Exp: $expirationDateFormatted"

            val currentDate = LocalDateTime.now()
            val expirationDate = itemJobDTO.expirationDate

            if (expirationDate.isBefore(currentDate)) {
                binding.tvStatus.text = "Expired"
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.tvStatus.context, R.color.red_700))
                binding.tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.tvStatus.context, R.color.red_300))
            } else {
                binding.tvStatus.text = "Active"
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.tvStatus.context, R.color.green_500))
                binding.tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.tvStatus.context, R.color.green_200))
            }

            if (isJobSeeker(binding.root.context)) {
                binding.btnApply.visibility = View.VISIBLE
            } else {
                binding.btnApply.visibility = View.GONE
            }

            binding.btnApply.setOnClickListener {
                Toast.makeText(binding.root.context, "Applied for ${itemJobDTO.title}", Toast.LENGTH_SHORT).show()
            }

            binding.root.setOnClickListener {
                onItemClick(itemJobDTO)
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

    private fun formatJobType(jobType: JobType): String {
        return jobType.name.split("_")
            .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
    }

    private fun isJobSeeker(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("currentUser", "")
        val gson = Gson()

        val currentUser = gson.fromJson(json, UserDTO::class.java)

        return currentUser?.role == Role.JOB_SEEKER
    }

}