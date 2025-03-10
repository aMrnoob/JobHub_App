package com.example.jobhub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.jobhub.databinding.ChooseJobBinding
import com.example.jobhub.databinding.ChooseProfileBinding
import com.example.jobhub.databinding.ProfileBinding
import com.example.jobhub.dto.auth.LoginResponse
import com.example.jobhub.entity.Job
import com.example.jobhub.entity.User
import com.example.jobhub.entity.enumm.Role

class ChooseProfileActivity : AppCompatActivity() {

    private lateinit var bindingChooseProfile: ChooseProfileBinding
    private lateinit var bindingChooseJob: ChooseJobBinding
    private lateinit var bindingProfile: ProfileBinding
    private lateinit var binding: ChooseProfileBinding

    private lateinit var user: User
    private lateinit var job: Job
    private lateinit var loginResponse: LoginResponse

    private var currentStep = 1
    private var lastClickTime: Long = 0

    private val jobTitleMap: Map<String, TextView> by lazy {
        mapOf(
            "Writer" to bindingChooseJob.tvWriter,
            "ArtDesign" to bindingChooseJob.tvArtDesign,
            "HR" to bindingChooseJob.tvHR,
            "Programer" to bindingChooseJob.tvProgramer,
            "Finance" to bindingChooseJob.tvFinance,
            "CustomerService" to bindingChooseJob.tvCustomerService,
            "FoodRestaurant" to bindingChooseJob.tvFoodRestaurant,
            "MusicProducer" to bindingChooseJob.tvMusicProducer
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = resources.getColor(android.R.color.black)

        val loginResponse = intent.getParcelableExtra<LoginResponse>("loginResponse")

        binding = ChooseProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvFindingJob.setOnClickListener {
            handleClick(it, Role.JOB_SEEKER)
        }

        binding.tvFindingStaff.setOnClickListener {
            handleClick(it, Role.EMPLOYER)
        }
    }

    private fun showStep(step: Int) {
        when (step) {
            1 -> {
                bindingChooseProfile = ChooseProfileBinding.inflate(layoutInflater)
                setContentView(bindingChooseProfile.root)
                window.navigationBarColor = resources.getColor(android.R.color.black)

                bindingChooseProfile.tvFindingJob.setOnClickListener {
                    handleClick(it, Role.EMPLOYER)
                }

                bindingChooseProfile.tvFindingStaff.setOnClickListener {
                    handleClick(it, Role.JOB_SEEKER)
                }
            }
            2 -> {
                bindingChooseJob = ChooseJobBinding.inflate(layoutInflater)
                setContentView(bindingChooseJob.root)
                window.navigationBarColor = resources.getColor(android.R.color.black)

                bindingChooseJob.btnComeBack.setOnClickListener {
                    currentStep = 1
                    showStep(currentStep)
                }

                jobSelectionListeners()

                bindingChooseJob.btnNext.setOnClickListener {
                    currentStep = 3
                    showStep(currentStep)
                }
            }
            3 -> {
                bindingProfile = ProfileBinding.inflate(layoutInflater)
                setContentView(bindingProfile.root)
                window.navigationBarColor = resources.getColor(android.R.color.black)

                /*bindingProfile.btnComeBack.setOnClickListener {
                    currentStep = 1
                    showStep(currentStep)
                }

                bindingProfile.btnNext.setOnClickListener {
                    // Handle profile information input logic here
                    currentStep = 4
                    showStep(currentStep)
                }*/
            }
            4 -> {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("loginResponse", loginResponse)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun handleClick(view: View, selectedRole: Role) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastClickTime < 300) {
            user.role = selectedRole
            selectRole()
        } else {
            toggleSelection(view)
        }

        lastClickTime = currentTime
    }

    private fun toggleSelection(view: View) {
        binding.tvFindingJob.isSelected = false
        binding.tvFindingStaff.isSelected = false
        view.isSelected = true
    }

    private fun selectRole() {
        if (user.role == Role.EMPLOYER) {
            currentStep = 2
            showStep(currentStep)
        } else {
            currentStep = 3
            showStep(currentStep)
        }
    }

    private fun jobSelectionListeners() {
        jobTitleMap.forEach { (_, textView) ->
            textView.setOnClickListener {
                val jobTitle = textView.text.toString()
                job.title = jobTitle
                highlightSelectedJob(textView)
            }
        }
    }

    private fun highlightSelectedJob(selectedTextView: TextView) {
        jobTitleMap.values.forEach { it.isSelected = false }
        selectedTextView.isSelected = true
    }
}