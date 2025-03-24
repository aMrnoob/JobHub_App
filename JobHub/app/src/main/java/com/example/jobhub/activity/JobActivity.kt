package com.example.jobhub.activity

import android.os.Bundle
import android.widget.Toast
import com.example.jobhub.adapter.SkillAdapter
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityAboutJobBinding
import com.example.jobhub.databinding.ActivityDetailJobBinding
import com.example.jobhub.databinding.ActivityRequirementJobBinding
import com.example.jobhub.dto.employer.JobInfo
import com.example.jobhub.dto.jobseeker.SkillInfo
import com.example.jobhub.entity.enumm.JobType
import com.example.jobhub.service.JobService

class JobActivity : BaseActivity() {

    private lateinit var bindingAboutJob: ActivityAboutJobBinding
    private lateinit var bindingDetailJob: ActivityDetailJobBinding
    private lateinit var bindingRequirementJob: ActivityRequirementJobBinding
    private val jobService: JobService by lazy {
        RetrofitClient.createRetrofit().create(JobService::class.java)
    }

    private lateinit var jobInfo: JobInfo
    private lateinit var skillAdapter: SkillAdapter
    private var currentStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingAboutJob = ActivityAboutJobBinding.inflate(layoutInflater)
        bindingDetailJob = ActivityDetailJobBinding.inflate(layoutInflater)
        bindingRequirementJob = ActivityRequirementJobBinding.inflate(layoutInflater)

        showStep(currentStep)
    }

    private fun showStep(step: Int) {
        when (step) {
            1 -> {
                setContentView(bindingAboutJob.root)

                bindingAboutJob.btnComeBack.setOnClickListener {
                    finish()
                }

                val title = bindingAboutJob.edtTitle.text.toString().trim()
                val description = bindingAboutJob.edtDescription.text.toString().trim()
                val requirements = bindingAboutJob.edtRequirements.text.toString().trim()
                val salary = bindingAboutJob.edtSalary.text.toString().trim()
                val location = bindingAboutJob.edtLocation.text.toString().trim()

                jobInfo.apply {
                    this.title = title
                    this.description = description
                    this.requirements = requirements
                    this.salary = "$salary$"
                    this.location = location
                }

                if (!isValidInput(title, description, requirements, salary, location)) {
                    Toast.makeText(this, "Please fill in the information completely!", Toast.LENGTH_SHORT).show()
                }

                currentStep = 2
                showStep(currentStep)
            }
            2 -> {
                setContentView(bindingDetailJob.root)

                bindingDetailJob.btnComeBack.setOnClickListener {
                    currentStep = 1
                    showStep(currentStep)
                }

                bindingDetailJob.btnNext.setOnClickListener {
                    val postingDate = bindingDetailJob.edtPostingDate.text.toString().trim()
                    val expirationDate = bindingDetailJob.edtExpirationDate.text.toString().trim()
                    val experience = bindingDetailJob.spinnerExperience.selectedItem.toString()
                    val company = bindingDetailJob.spinnerCompany.selectedItem.toString()
                    val jobType = bindingDetailJob.spinnerJobType.selectedItem.toString()

                    if (!isValidInput(postingDate, expirationDate, experience, company, jobType)) {
                        Toast.makeText(this, "Please fill in the information completely!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    jobInfo.apply {
                        this.postingDate = postingDate
                        this.expirationDate = expirationDate
                        this.experienceRequired = experience
                        //this.companyInfo = company
                        this.jobType = try {
                            JobType.valueOf(jobType)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }

                    currentStep = 3
                    showStep(currentStep)
                }
            }
            3 -> {
                setContentView(bindingRequirementJob.root)

                setupRecyclerView()

                bindingRequirementJob.btnComeBack.setOnClickListener {
                    currentStep = 2
                    showStep(currentStep)
                }

                bindingRequirementJob.btnComplete.setOnClickListener {
                    jobInfo.requiredSkills = skillAdapter.getSkills().toSet()
                    jobInfo.applications = emptySet()

                    if (jobInfo.requiredSkills.isEmpty()) {
                        Toast.makeText(this, "Please add at least one skill!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    Toast.makeText(this, "Job Info Saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }

            }
        }
    }

    private fun setupRecyclerView() {
        skillAdapter = SkillAdapter(jobInfo.requiredSkills.toMutableList())
        bindingRequirementJob.rvSkillJob.adapter = skillAdapter

        bindingRequirementJob.tvAddSkill.setOnClickListener {
            skillAdapter.addSkill(SkillInfo(
                skillName = "",
                skillId = 0,
                users = null,
                jobs = setOf(jobInfo)
            ))
        }
    }

    /*private fun saveJobToDatabase(jobInfo: JobInfo) {
        jobService.createJob(jobInfo).enqueue(object : Callback<JobInfo> {
            override fun onResponse(call: Call<Void>, response: ApiResponse<Void>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(this@JobActivity, "Job saved successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@JobActivity, "Failed to save job", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JobInfo>, t: Throwable) {
                Toast.makeText(this@JobActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }*/

    private fun isValidInput(vararg fields: String): Boolean {
        return fields.all { it.isNotBlank() }
    }
}