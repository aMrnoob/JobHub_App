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
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainRequirementsBinding
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.dto.SkillDTO
import com.example.jobhub.entity.Job
import com.example.jobhub.fragment.fragmentinterface.FragmentInterface
import com.example.jobhub.service.SkillService
import com.google.gson.Gson

class RequirementsFragment : Fragment() {

    private var _binding: MainRequirementsBinding? = null
    private val binding get() = _binding!!
    private var itemJobDTO: ItemJobDTO? = null
    private var jobRequirementsInterface: FragmentInterface? = null
    private lateinit var skillAdapter: SkillAdapter
    private val skillService: SkillService by lazy {
        RetrofitClient.createRetrofit().create(SkillService::class.java)
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
        _binding = MainRequirementsBinding.inflate(inflater, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val jobJson = sharedPreferences.getString("job", null)

        if (!jobJson.isNullOrEmpty()) {
            itemJobDTO = Gson().fromJson(jobJson, ItemJobDTO::class.java)
        }

        setupRecyclerView()
        setEditTextEnabled(false)

        binding.btnUpdate.setOnClickListener {
            setEditTextEnabled(false)
            update()
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    fun enableEditing() {
        setEditTextEnabled(true)
        skillAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        val skillsList = itemJobDTO?.requiredSkills?.toMutableList() ?: mutableListOf()

        skillAdapter = SkillAdapter(skillsList, false)
        binding.rvSkillJob.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSkillJob.adapter = skillAdapter

        binding.tvAddSkill.setOnClickListener {
            val newSkill = SkillDTO(
                skillName = "",
                skillId = skillAdapter.itemCount + 1
            )

            skillAdapter.addSkill(newSkill)
        }
    }

    private fun setEditTextEnabled(enabled: Boolean) {
        skillAdapter.setEditable(enabled)
        binding.tvAddSkill.isEnabled = enabled
        binding.btnUpdate.isEnabled = enabled
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun update() {
        val job = Job()
        if (itemJobDTO == null) {
            return
        }

        job.jobId = itemJobDTO!!.jobId
        job.requiredSkills = skillAdapter.getSkills().toSet()

        ApiHelper().callApi(
            context = requireContext(),
            call = skillService.updateSkills(job.jobId, job.requiredSkills!!),
            onSuccess = {
                itemJobDTO?.requiredSkills = skillAdapter.getSkillsDTO().toSet()
                saveJobToPrefs()
            }
        )
    }

    private fun saveJobToPrefs() {
        val sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)
        val jobJson = Gson().toJson(itemJobDTO)
        sharedPreferences.edit().putString("job", jobJson).apply()
    }
}