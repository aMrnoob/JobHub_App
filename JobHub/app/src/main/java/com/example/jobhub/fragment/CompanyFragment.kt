package com.example.jobhub.fragment

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.activity.CompanyActivity
import com.example.jobhub.adapter.CompanyAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainCompanyBinding
import com.example.jobhub.entity.Company
import com.example.jobhub.service.CompanyService

class CompanyFragment : Fragment() {

    private var _binding: MainCompanyBinding? = null
    private val binding get() = _binding!!

    private val companyService: CompanyService by lazy {
        RetrofitClient.createRetrofit().create(CompanyService::class.java)
    }
    private lateinit var companyAdapter: CompanyAdapter
    private var companyList: MutableList<Company> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainCompanyBinding.inflate(inflater, container, false)

        binding.ivAddCompany.setOnClickListener {
            animateView(it)
            val intent = Intent(requireContext(), CompanyActivity::class.java)
            startActivity(intent)
        }

        setupRecyclerView()
        fetchAllCompanies()

        return binding.root
    }

    private fun setupRecyclerView() {
        companyAdapter = CompanyAdapter(companyList)
        binding.rvCompany.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = companyAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchAllCompanies() {
        val token = getAuthToken() ?: return

        ApiHelper().callApi(
            context = requireContext(),
            call = companyService.getAllCompaniesByUserId("Bearer $token"),
            onSuccess = { response ->
                companyList.apply {
                    clear()
                    response?.let { addAll(it) }
                }
                companyAdapter.notifyDataSetChanged()
            }
        )
    }

    private fun getAuthToken(): String? {
        return requireContext().getSharedPreferences("JobHubPrefs", AppCompatActivity.MODE_PRIVATE)
            .getString("authToken", null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun animateView(view: View) {
        ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f)
        ).apply {
            duration = 300
            start()
        }
    }
}