package com.example.jobhub.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jobhub.R
import com.example.jobhub.databinding.ItemApplicationBinding
import com.example.jobhub.dto.ApplicationDTO
import android.util.Base64
import com.example.jobhub.entity.enumm.ApplicationAction

class ApplicationAdapter(
    private val applications: List<ApplicationDTO>,
    private val onActionClick: ((ApplicationDTO, ApplicationAction) -> Unit)? = null
) : RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder>() {

    inner class ApplicationViewHolder(val binding: ItemApplicationBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(application: ApplicationDTO) {
            with(binding) {
                tvFullName.text = application.userDTO.fullName
                tvStatus.text = application.status.name
                tvEmail.text = "Email: " + application.userDTO.email
                tvPhone.text = "Phone number: " + application.userDTO.phone

                val base64Image = application.userDTO.imageUrl
                if (!base64Image.isNullOrEmpty()) {
                    try {
                        val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        binding.userAvatar.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        Log.e("ApplicationAdapter", "Lỗi decode base64: ${e.message}")
                        binding.userAvatar.setImageResource(R.drawable.icon_upload_image)
                    }
                } else {
                    binding.userAvatar.setImageResource(R.drawable.icon_upload_image)
                }

                binding.btnDownloadCV.setOnClickListener {
                    onActionClick?.invoke(application, ApplicationAction.DOWNLOAD_CV)
                }

                binding.btnSeeResume.setOnClickListener {
                    onActionClick?.invoke(application, ApplicationAction.SEE_RESUME)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val binding = ItemApplicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ApplicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(applications[position])
    }

    override fun getItemCount(): Int = applications.size
}
