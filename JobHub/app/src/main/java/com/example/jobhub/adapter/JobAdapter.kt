package com.example.jobhub.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.activity.ApplicantActivity
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.ItemJobBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.entity.enumm.ApplicationAction
import com.example.jobhub.entity.enumm.JobAction
import com.example.jobhub.entity.enumm.JobType
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.utils.ResumeViewerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JobAdapter(
    private val jobList: List<ItemJobDTO>,
    private val onActionClick: ((ItemJobDTO, JobAction) -> Unit)? = null
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    private var expandedPosition = RecyclerView.NO_POSITION

    inner class JobViewHolder(private val binding: ItemJobBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
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

                when (getUserRole(root.context)) {
                    Role.JOB_SEEKER -> {
                        layoutApply.visibility = View.VISIBLE
                        layoutActions.visibility = View.GONE
                    }
                    Role.EMPLOYER -> {
                        layoutApply.visibility = View.GONE
                        layoutActions.visibility = View.VISIBLE
                    }
                    else -> {
                        layoutApply.visibility = View.GONE
                        layoutActions.visibility = View.GONE
                    }
                }

                val isExpanded = position == expandedPosition
                rvResumes.visibility = if (isExpanded) View.VISIBLE else View.GONE
                tvListResume.visibility = if (isExpanded && getUserRole(root.context) == Role.EMPLOYER) View.VISIBLE else View.GONE

                rvResumes.apply {
                    layoutManager = LinearLayoutManager(root.context)
                    adapter = ApplicationAdapter(
                        itemJobDTO.applications,
                        onActionClick = { application, action ->
                            when (action) {
                                ApplicationAction.DOWNLOAD_CV -> {
                                    val resumeUrl = application.resumeUrl
                                    if (resumeUrl.isNotEmpty()) {
                                        openResume(root.context, resumeUrl)
                                    }
                                }
                                ApplicationAction.SEE_RESUME -> {
                                    SharedPrefsManager(context).saveCurrentApplication(application)
                                    context.startActivity(Intent(binding.root.context, ApplicantActivity::class.java))
                                }
                            }
                        }
                    )
                }

                root.setOnClickListener {
                    val previousExpandedPosition = expandedPosition
                    expandedPosition = if (isExpanded) {
                        RecyclerView.NO_POSITION
                    } else {
                        position
                    }
                    notifyItemChanged(previousExpandedPosition)
                    notifyItemChanged(expandedPosition)

                    handleAction(itemJobDTO, JobAction.CLICK)
                }
                btnBookmark.setOnClickListener { handleAction(itemJobDTO, JobAction.BOOKMARK) }
                btnApply.setOnClickListener { handleAction(itemJobDTO, JobAction.APPLY) }
                btnEdit.setOnClickListener { handleAction(itemJobDTO, JobAction.EDIT) }
                btnRemove.setOnClickListener { handleAction(itemJobDTO, JobAction.DELETE) }
            }
        }

        private fun handleAction(item: ItemJobDTO, action: JobAction) { onActionClick?.invoke(item, action) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    private fun getUserRole(context: Context): Role? {
        val sharedPrefs = SharedPrefsManager(context)
        return sharedPrefs.role
    }

    private fun openResume(viewContext: Context, url: String) {
        val fullUrl = if (url.startsWith("./") || url.startsWith("../")) {
            val baseUrl = RetrofitClient.getBaseUrl()
            val relativePath = url.replaceFirst("./", "")
            "$baseUrl$relativePath"
        } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
            val baseUrl = RetrofitClient.getBaseUrl()
            "$baseUrl$url"
        } else {
            url
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                ResumeViewerUtils.downloadAndOpenResume(viewContext, fullUrl)
            } catch (e: Exception) {
                Toast.makeText(viewContext, "Không thể mở CV: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}