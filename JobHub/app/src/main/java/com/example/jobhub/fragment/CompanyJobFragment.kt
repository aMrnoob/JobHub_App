package com.example.jobhub.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.jobhub.R
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.MainCompanyJobBinding
import com.example.jobhub.dto.CompanyDTO
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.entity.enumm.Role
import com.example.jobhub.fragment.fragmentinterface.FragmentInterface
import com.example.jobhub.service.CompanyService

class CompanyJobFragment : Fragment() {

    private lateinit var sharedPrefs: SharedPrefsManager

    private var jobDTO: ItemJobDTO? = null
    private var jobRequirementsInterface: FragmentInterface? = null
    private var _binding: MainCompanyJobBinding? = null

    private val binding get() = _binding!!
    private val companyService: CompanyService by lazy { RetrofitClient.createRetrofit().create(CompanyService::class.java) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentInterface) { jobRequirementsInterface = context }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainCompanyJobBinding.inflate(inflater, container, false)
        sharedPrefs = SharedPrefsManager(requireContext())

        val role = sharedPrefs.role
        if(role == Role.JOB_SEEKER) { binding.btnUpdate.visibility = View.GONE }

        jobDTO = sharedPrefs.getCurrentJob()

        displayJob()
        setEditTextEnabled(false)

        binding.btnUpdate.setOnClickListener {
            update()
            setEditTextEnabled(false)
        }

        return binding.root
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
        } else { binding.edtDescription.setOnClickListener(null) }

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

    private fun update() {
        if (jobDTO == null) { return }

        val companyDTO = CompanyDTO()
        companyDTO.companyId = jobDTO!!.company.companyId
        companyDTO.companyName = binding.edtCompanyName.text.toString()
        companyDTO.description = binding.edtDescription.text.toString()

        ApiHelper().callApi(
            context = requireContext(),
            call = companyService.updateCompany(companyDTO),
            onSuccess = {
                jobDTO!!.company = jobDTO!!.company.copy(
                    companyName = companyDTO.companyName.toString(),
                    description = companyDTO.description.toString()
                )
                jobDTO?.let { sharedPrefs.saveCurrentJob(it) }
            }
        )
    }

    fun enableEditing() {
        _binding?.let {
            setEditTextEnabled(true)
        }
    }
}