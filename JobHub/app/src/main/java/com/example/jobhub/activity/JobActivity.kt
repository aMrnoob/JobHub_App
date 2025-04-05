package com.example.jobhub.activity

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
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
import com.example.jobhub.dto.JobDTO
import com.example.jobhub.dto.SkillDTO
import com.example.jobhub.entity.Company
import com.example.jobhub.entity.Job
import com.example.jobhub.entity.enumm.JobType
import com.example.jobhub.service.CompanyService
import com.example.jobhub.service.JobService
import com.example.jobhub.service.SkillService
import com.example.jobhub.validation.ValidationResult
import com.example.jobhub.validation.validateSalary
import com.example.jobhub.validation.validateTitle
import java.util.Calendar

class JobActivity : BaseActivity() {

    private lateinit var bindingAboutJob: ActivityAboutJobBinding
    private lateinit var bindingDetailJob: ActivityDetailJobBinding
    private lateinit var bindingRequirementJob: ActivityRequirementJobBinding
    private val jobService: JobService by lazy {
        RetrofitClient.createRetrofit().create(JobService::class.java)
    }
    private val skillService: SkillService by lazy {
        RetrofitClient.createRetrofit().create(SkillService::class.java)
    }
    private val companyService: CompanyService by lazy {
        RetrofitClient.createRetrofit().create(CompanyService::class.java)
    }

    private var jobDTO: JobDTO = JobDTO()
    private lateinit var skillAdapter: SkillAdapter
    private var companyList: MutableList<Company> = mutableListOf()
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

                    if (!validateField(validateTitle(title))) return@setOnClickListener
                    if (!validateField(validateSalary(salary))) return@setOnClickListener

                    if (!isValidInput(description, requirements, salary, location)) {
                        Toast.makeText(this, "Please fill in the information completely!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    } else {
                        jobDTO = jobDTO.copy(
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
                setupDatePickers()

                bindingDetailJob.btnComeBack.setOnClickListener {
                    currentStep = 1
                    showStep(currentStep)
                }

                bindingDetailJob.btnNext.setOnClickListener {
                    val postingDateStr = bindingDetailJob.edtPostingDate.text.toString().trim() + "T00:00:00"
                    val expirationDateStr = bindingDetailJob.edtExpirationDate.text.toString().trim() + "T23:59:59"
                    val experience = bindingDetailJob.spinnerExperience.selectedItem.toString()
                    val company = bindingDetailJob.spinnerCompany.selectedItem.toString()
                    val jobType = bindingDetailJob.spinnerJobType.selectedItem.toString()

                    if (!isValidInput(postingDateStr, expirationDateStr, experience, company, jobType)) {
                        Toast.makeText(this, "Please fill in the information completely!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    jobDTO = jobDTO.copy(
                        companyName = company,
                        postingDate = postingDateStr,
                        expirationDate = expirationDateStr,
                        experienceRequired = experience,
                        jobType = JobType.entries.find { it.name == jobType.replace(" ", "_").uppercase() }
                    )

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
                    jobDTO.requiredSkills = skillAdapter.getSkillsDTO().toSet()

                    if (jobDTO.requiredSkills.isEmpty()) {
                        Toast.makeText(this, "Please add at least one skill!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    createJob()
                    updateSkill()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        skillAdapter = SkillAdapter(jobDTO.requiredSkills.toMutableList(), true)
        bindingRequirementJob.rvSkillJob.layoutManager = LinearLayoutManager(this)
        bindingRequirementJob.rvSkillJob.adapter = skillAdapter

        bindingRequirementJob.tvAddSkill.setOnClickListener {
            val newSkill = SkillDTO(
                skillName = "",
                skillId = skillAdapter.itemCount + 1
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
                    jobDTO.companyName = selectedCompany.companyName
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateListener = { editText: EditText ->
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                editText.setText(formattedDate)
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
                    companyList = it as MutableList<Company>
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

    private fun createJob() {
        ApiHelper().callApi(
            context = this,
            call = jobService.createJob(jobDTO),
            onSuccess = {
                finish()
            }
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateSkill() {
        val job = Job()

        job.jobId = jobDTO.jobId!!
        job.requiredSkills = skillAdapter.getSkills().toSet()

        ApiHelper().callApi(
            context = this,
            call = skillService.updateSkills(job.jobId, job.requiredSkills!!),
            onSuccess = {}
        )
    }

    private fun validateField(validationResult: ValidationResult): Boolean {
        return when (validationResult) {
            is ValidationResult.Error -> {
                Toast.makeText(this@JobActivity, validationResult.message, Toast.LENGTH_SHORT).show()
                false
            }
            is ValidationResult.Success -> true
        }
    }
}