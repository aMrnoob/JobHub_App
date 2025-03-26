package com.example.jobhub.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.jobhub.R
import com.example.jobhub.databinding.MainCompanyJobBinding
import com.example.jobhub.databinding.MainRequirementsBinding
import com.example.jobhub.dto.employer.JobInfo
import com.example.jobhub.fragment.fragmentinterface.FragmentInterface
import com.google.gson.Gson

class CompanyJobFragment : Fragment() {

    private var _binding: MainCompanyJobBinding? = null
    private val binding get() = _binding!!
    private var jobInfo: JobInfo? = null
    private var jobRequirementsInterface: FragmentInterface? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentInterface) {
            jobRequirementsInterface = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MainCompanyJobBinding.inflate(inflater, container, false)

        loadJobInfoFromPrefs()
        displayJobInfo()
        setEditTextEnabled(false)

        return binding.root
    }

    fun enableEditing() {
        setEditTextEnabled(true)
    }

    private fun displayJobInfo() {
        jobInfo?.companyInfo?.let { company ->
            binding.edtCompanyName.setText(company.companyName)
            binding.edtAddress.setText(company.address)
            binding.edtLogoUrl.setText(company.logoUrl)
            binding.edtWebsite.setText(company.website)
            binding.edtDescription.setText(company.description)
        }
    }

    private fun setEditTextEnabled(enabled: Boolean) {
        setEditTextState(binding.edtCompanyName, enabled)
        setEditTextState(binding.edtAddress, enabled)
        setEditTextState(binding.edtLogoUrl, enabled)
        setEditTextState(binding.edtWebsite, enabled)
        setEditTextState(binding.edtDescription, enabled)
        if (!enabled) {
            binding.edtDescription.setOnClickListener {
                showDescriptionDialog(binding.edtDescription.text.toString())
            }
        } else {
            binding.edtDescription.setOnClickListener(null)
        }

        binding.btnAdd.isEnabled = enabled
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

    private fun loadJobInfoFromPrefs() {
        val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val jobInfoJson = sharedPreferences.getString("job_info", null)

        if (!jobInfoJson.isNullOrEmpty()) {
            jobInfo = Gson().fromJson(jobInfoJson, JobInfo::class.java)
        }
    }
}