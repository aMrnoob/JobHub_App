package com.example.jobhub.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.adapter.SkillAdapter
import com.example.jobhub.databinding.MainRequirementsBinding
import com.example.jobhub.dto.employer.JobInfo
import com.example.jobhub.dto.jobseeker.SkillInfo
import com.example.jobhub.fragment.fragmentinterface.FragmentInterface
import com.google.gson.Gson

class RequirementsFragment : Fragment() {

    private var _binding: MainRequirementsBinding? = null
    private val binding get() = _binding!!
    private var jobInfo: JobInfo? = null
    private var jobRequirementsInterface: FragmentInterface? = null
    private lateinit var skillAdapter: SkillAdapter

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
        _binding = MainRequirementsBinding.inflate(inflater, container, false)

        loadJobInfoFromPrefs()
        setupRecyclerView()
        setEditTextEnabled(false)

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    fun enableEditing() {
        setEditTextEnabled(true)
        skillAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        val skillsList = jobInfo?.requiredSkills?.toMutableList() ?: mutableListOf()

        skillAdapter = SkillAdapter(skillsList, false)
        binding.rvSkillJob.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSkillJob.adapter = skillAdapter

        binding.tvAddSkill.setOnClickListener {
            val newSkill = SkillInfo(
                skillName = "",
                skillId = skillAdapter.itemCount + 1,
                users = null,
            )

            skillAdapter.addSkill(newSkill)
        }
    }

    private fun setEditTextEnabled(enabled: Boolean) {
        skillAdapter.setEditable(enabled)
        binding.tvAddSkill.isEnabled = enabled
        binding.btnComplete.isEnabled = enabled
    }

    private fun loadJobInfoFromPrefs() {
        val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val jobInfoJson = sharedPreferences.getString("job_info", null)

        if (!jobInfoJson.isNullOrEmpty()) {
            jobInfo = Gson().fromJson(jobInfoJson, JobInfo::class.java)
        }
    }
}