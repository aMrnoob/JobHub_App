package com.example.jobhub.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainCompanyJobBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.dto.toCompany
import com.example.jobhub.entity.Job
import com.example.jobhub.fragment.fragmentinterface.FragmentInterface
import com.example.jobhub.service.CompanyService
import com.google.gson.Gson

class CompanyJobFragment : Fragment() {

    private var _binding: MainCompanyJobBinding? = null
    private val binding get() = _binding!!
    private var jobDTO: ItemJobDTO? = null
    private var jobRequirementsInterface: FragmentInterface? = null
    private val companyService: CompanyService by lazy {
        RetrofitClient.createRetrofit().create(CompanyService::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentInterface) {
            jobRequirementsInterface = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainCompanyJobBinding.inflate(inflater, container, false)

        loadJobFromPrefs()
        displayJob()
        setEditTextEnabled(false)

        binding.btnUpdate.setOnClickListener {
            update()
            setEditTextEnabled(false)
        }

        return binding.root
    }

    fun enableEditing() {
        setEditTextEnabled(true)
    }

    private fun displayJob() {
        jobDTO?.company?.let { company ->
            Glide.with(binding.ivImgJob)
                .load(jobDTO!!.company.logoUrl)
                .placeholder(R.drawable.error_image)
                .error(R.drawable.error_image)
                .into(binding.ivImgJob)
            binding.edtCompanyName.setText(company.companyName)
            binding.edtDescription.setText(company.description)
        }
    }

    private fun setEditTextEnabled(enabled: Boolean) {
        setEditTextState(binding.edtCompanyName, enabled)
        setEditTextState(binding.edtDescription, enabled)
        if (!enabled) {
            binding.edtDescription.setOnClickListener {
                showDescriptionDialog(binding.edtDescription.text.toString())
            }
        } else {
            binding.edtDescription.setOnClickListener(null)
        }

        binding.btnUpdate.isEnabled = enabled
    }

    private fun setEditTextState(editText: EditText, enabled: Boolean) {
        editText.isFocusable = enabled
        editText.isFocusableInTouchMode = enabled
        editText.isCursorVisible = enabled
    }

    private fun showDescriptionDialog(description: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Description")
        builder.setMessage(description)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun loadJobFromPrefs() {
        val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val jobJson = sharedPreferences.getString("job", null)

        if (!jobJson.isNullOrEmpty()) {
            jobDTO = Gson().fromJson(jobJson, ItemJobDTO::class.java)
        }
    }

    private fun update() {
        if (jobDTO == null) {
            return
        }

        val updatedCompany = jobDTO!!.company.toCompany().copy(
            companyName = binding.edtCompanyName.text.toString(),
            description = binding.edtDescription.text.toString()
        )

        ApiHelper().callApi(
            context = requireContext(),
            call = companyService.updateCompany(updatedCompany),
            onSuccess = {
                jobDTO!!.company = jobDTO!!.company.copy(
                    companyName = updatedCompany.companyName.toString(),
                    description = updatedCompany.description.toString()
                )
                saveJobToPrefs()
            }
        )
    }

    private fun saveJobToPrefs() {
        val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val jobJson = Gson().toJson(jobDTO)
        sharedPreferences.edit().putString("job", jobJson).apply()
    }
}