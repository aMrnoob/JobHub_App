package com.example.jobhub.fragment

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.activity.CompanyActivity
import com.example.jobhub.adapter.CompanyAdapter
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainCompanyBinding
import com.example.jobhub.dto.employer.CompanyInfo
import com.example.jobhub.model.ApiResponse
import com.example.jobhub.service.CompanyService
import com.example.jobhub.service.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CompanyFragment : Fragment() {

    private var _binding: MainCompanyBinding? = null
    private val binding get() = _binding!!

    private val companyService: CompanyService by lazy {
        RetrofitClient.createRetrofit().create(CompanyService::class.java)
    }
    private lateinit var companyAdapter: CompanyAdapter
    private var companyList: MutableList<CompanyInfo> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

    private fun fetchAllCompanies() {
        val token = getAuthToken()
        if (token == null) {
            Toast.makeText(requireContext(), "You are not logged in yet!", Toast.LENGTH_SHORT).show()
            return
        }

        companyService.getAllCompanies(token).enqueue(object :
            Callback<ApiResponse<List<CompanyInfo>>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<ApiResponse<List<CompanyInfo>>>,
                response: Response<ApiResponse<List<CompanyInfo>>>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    companyList.clear()
                    response.body()?.data?.let { companyList.addAll(it) }
                    companyAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<CompanyInfo>>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error connection: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("API_ERROR", "Failed to get all companies", t)
            }
        })
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}