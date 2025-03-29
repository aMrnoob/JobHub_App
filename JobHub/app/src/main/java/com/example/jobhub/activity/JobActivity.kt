package com.example.jobhub.activity

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.adapter.SkillAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.ActivityAboutJobBinding
import com.example.jobhub.databinding.ActivityDetailJobBinding
import com.example.jobhub.databinding.ActivityRequirementJobBinding
import com.example.jobhub.dto.employer.CompanyInfo
import com.example.jobhub.dto.employer.JobInfo
import com.example.jobhub.dto.jobseeker.SkillInfo
import com.example.jobhub.entity.enumm.JobType
import com.example.jobhub.service.CompanyService
import com.example.jobhub.service.JobService
import java.util.Calendar

class JobActivity : BaseActivity() {

    private lateinit var bindingAboutJob: ActivityAboutJobBinding
    private lateinit var bindingDetailJob: ActivityDetailJobBinding
    private lateinit var bindingRequirementJob: ActivityRequirementJobBinding
    private val jobService: JobService by lazy {
        RetrofitClient.createRetrofit().create(JobService::class.java)
    }
    private val companyService: CompanyService by lazy {
        RetrofitClient.createRetrofit().create(CompanyService::class.java)
    }

    private var jobInfo: JobInfo = JobInfo()
    private lateinit var skillAdapter: SkillAdapter
    private var companyList: MutableList<CompanyInfo> = mutableListOf()
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

                bindingAboutJob.btnNext.setOnClickListener {
                    val title = bindingAboutJob.edtTitle.text.toString().trim()
                    val description = bindingAboutJob.edtDescription.text.toString().trim()
                    val requirements = bindingAboutJob.edtRequirements.text.toString().trim()
                    val salary = bindingAboutJob.edtSalary.text.toString().trim()
                    val location = bindingAboutJob.edtLocation.text.toString().trim()

                    if (!isValidInput(title, description, requirements, salary, location)) {
                        Toast.makeText(this, "Please fill in the information completely!", Toast.LENGTH_SHORT).show()
                    } else {
                        jobInfo = jobInfo.copy(
                            title = title,
                            description = description,
                            requirements = requirements,
                            salary = "$salary$",
                            location = location
                        )

                        currentStep = 2
                        showStep(currentStep)
                    }
                }
            }
            2 -> {
                bindingDetailJob = ActivityDetailJobBinding.inflate(layoutInflater)
                setContentView(bindingDetailJob.root)

                getAllCompaniesByUserId()

                bindingDetailJob.btnComeBack.setOnClickListener {
                    currentStep = 1
                    showStep(currentStep)
                }

                setupDatePickers()

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
                        val formattedJobType = jobType.replace(" ", "_").uppercase()
                        this.jobType = JobType.entries.find { it.name == formattedJobType }
                    }

                    currentStep = 3
                    showStep(currentStep)
                }
            }
            3 -> {
                bindingRequirementJob = ActivityRequirementJobBinding.inflate(layoutInflater)
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

                    createJob(jobInfo)
                }
            }
        }
    }

    private fun setupRecyclerView() {

        skillAdapter = SkillAdapter(jobInfo.requiredSkills.toMutableList())
        bindingRequirementJob.rvSkillJob.layoutManager = LinearLayoutManager(this)
        bindingRequirementJob.rvSkillJob.adapter = skillAdapter

        bindingRequirementJob.tvAddSkill.setOnClickListener {
            Toast.makeText(this, "Add Skill Clicked", Toast.LENGTH_SHORT).show()
            val newSkill = SkillInfo(
                skillName = "",
                skillId = skillAdapter.itemCount + 1,
                users = null,
            )

            skillAdapter.addSkill(newSkill)
        }
    }

    private fun setupCompanySpinner() {
        val companyNames = companyList.map { it.companyName }

        val adapter = object : ArrayAdapter<String>(this, R.layout.simple_spinner_item, companyNames) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(ContextCompat.getColor(context, R.color.black))
                view.textSize = 17f

                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView

                val padding = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10f, context.resources.displayMetrics
                ).toInt()

                view.setPadding(view.paddingLeft, padding, view.paddingRight, padding)

                return view
            }
        }

        bindingDetailJob.spinnerCompany.adapter = adapter

        bindingDetailJob.spinnerCompany.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCompanyName = companyNames[position]
                val selectedCompany = companyList.find { it.companyName == selectedCompanyName }

                if (selectedCompany != null) {
                    jobInfo.companyInfo = selectedCompany
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateListener = { editText: EditText ->
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                editText.setText(date)
            }, year, month, day).show()
        }

        bindingDetailJob.edtPostingDate.setOnClickListener { dateListener(bindingDetailJob.edtPostingDate) }
        bindingDetailJob.edtExpirationDate.setOnClickListener { dateListener(bindingDetailJob.edtExpirationDate) }
    }


    private fun getAllCompaniesByUserId() {
        val token = getAuthToken() ?: return

        ApiHelper().callApi(
            context = this,
            call = companyService.getAllCompaniesByUserId("Bearer $token"),
            onSuccess = {
                if (it != null) {
                    companyList = it as MutableList<CompanyInfo>
                    setupCompanySpinner()
                }
            }
        )
    }

    private fun getAuthToken(): String? {
        return getSharedPreferences("JobHubPrefs", MODE_PRIVATE)
            .getString("authToken", null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun isValidInput(vararg fields: String): Boolean {
        return fields.all { it.isNotBlank() }
    }

    private fun createJob(jobInfo: JobInfo) {
        ApiHelper().callApi(
            context = this,
            call = jobService.createJob(jobInfo),
            onSuccess = {
                finish()
            }
        )
    }
}