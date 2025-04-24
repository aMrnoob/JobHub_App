package com.example.jobhub.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.anim.AnimationHelper
import com.example.jobhub.databinding.ItemJobBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.entity.enumm.JobAction
import com.example.jobhub.entity.enumm.JobType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BookmarkAdapter (
    private val jobList: List<ItemJobDTO>,
    private val onActionClick: ((ItemJobDTO, JobAction) -> Unit)? = null
) : RecyclerView.Adapter<BookmarkAdapter.JobViewHolder>() {

    inner class JobViewHolder(private val binding: ItemJobBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "ResourceAsColor")
        fun bind(itemJobDTO: ItemJobDTO) {
            with(binding) {
                Glide.with(root.context)
                    .load(itemJobDTO.company.logoUrl)
                    .placeholder(R.drawable.error_image)
                    .error(R.drawable.error_image)
                    .into(ivImgJob)

                tvTitle.text = itemJobDTO.title
                tvLocationJobType.text = "${itemJobDTO.location} - ${formatJobType(itemJobDTO.jobType)}"
                tvSalary.text = itemJobDTO.salary

                val formattedDate = itemJobDTO.expirationDate.format(DateTimeFormatter.ofPattern("d/M/yyyy"))
                tvPostExpirationDate.text = "Exp: $formattedDate"

                val isExpired = itemJobDTO.expirationDate.isBefore(LocalDateTime.now())
                tvStatus.text = if (isExpired) "Expired" else "Active"
                val (textColor, bgColor) = if (isExpired) {
                    R.color.red_700 to R.color.red_300
                } else {
                    R.color.green_500 to R.color.green_200
                }
                tvStatus.setTextColor(ContextCompat.getColor(root.context, textColor))
                tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(root.context, bgColor))

                layoutApply.visibility = View.VISIBLE
                layoutActions.visibility = View.GONE

                btnBookmark.text = "Remove"
                btnBookmark.setTextColor(ContextCompat.getColor(root.context, R.color.white))
                btnBookmark.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(root.context, R.color.red_400))

                root.setOnClickListener { handleAction(itemJobDTO, JobAction.CLICK) }
                btnBookmark.setOnClickListener { handleAction(itemJobDTO, JobAction.BOOKMARK) }
                btnApply.setOnClickListener { handleAction(itemJobDTO, JobAction.APPLY) }
            }
        }

        private fun handleAction(item: ItemJobDTO, action: JobAction) {
            AnimationHelper.animateScale(binding.root)
            onActionClick?.invoke(item, action)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkAdapter.JobViewHolder {
        val binding =
            ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkAdapter.JobViewHolder, position: Int) {
        holder.bind(jobList[position])
    }

    override fun getItemCount(): Int = jobList.size

    private fun formatJobType(jobType: JobType): String {
        return jobType.name.split("_")
            .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
    }
}