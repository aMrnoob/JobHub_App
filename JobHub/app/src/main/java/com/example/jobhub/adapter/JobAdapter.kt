package com.example.jobhub.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.databinding.ItemJobBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.entity.enumm.JobType
import com.example.jobhub.entity.enumm.Role
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JobAdapter(
    private val jobList: List<ItemJobDTO>,
    private val onItemClick: ((ItemJobDTO) -> Unit),
    private val onEditClick: ((ItemJobDTO) -> Unit)? = null,
    private val onDeleteClick: ((ItemJobDTO) -> Unit)? = null
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    private var lastShownPosition: Int = -1

    inner class JobViewHolder(private val binding: ItemJobBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun bind(itemJobDTO: ItemJobDTO, position: Int) {
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

            binding.btnApply.visibility = if (isJobSeeker(binding.root.context)) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.root.setOnClickListener {
                onItemClick(itemJobDTO)
            }

            binding.layoutActions.visibility = if (position == lastShownPosition) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                onItemClick(itemJobDTO)
                if (lastShownPosition != -1) {
                    val oldPosition = lastShownPosition
                    lastShownPosition = -1
                    notifyItemChanged(oldPosition)
                }
            }

            binding.root.setOnLongClickListener {
                lastShownPosition = if (lastShownPosition == position) -1 else position
                notifyDataSetChanged()
                true
            }

            binding.ivEdit.setOnClickListener {
                onEditClick?.let { it1 -> it1(itemJobDTO) }
            }

            binding.ivRemove.setOnClickListener {
                onDeleteClick?.let { it1 -> it1(itemJobDTO) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding =
            ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobList[position], position)
    }

    override fun getItemCount(): Int = jobList.size

    private fun formatJobType(jobType: JobType): String {
        return jobType.name.split("_")
            .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
    }

    private fun isJobSeeker(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("currentUser", null) ?: return false
        return try {
            val currentUser = Gson().fromJson(json, UserDTO::class.java)
            currentUser?.role == Role.JOB_SEEKER
        } catch (e: Exception) {
            false
        }
    }
}