package com.example.jobhub.fragment

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.activity.CompanyActivity
import com.example.jobhub.adapter.CompanyAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.MainCompanyBinding
import com.example.jobhub.entity.Company
import com.example.jobhub.service.CompanyService


class CompanyFragment : Fragment() {

    private lateinit var companyAdapter: CompanyAdapter
    private lateinit var sharedPrefs: SharedPrefsManager

    private var _binding: MainCompanyBinding? = null
    private var allCompanies: MutableList<Company> = mutableListOf()
    private var companyList: MutableList<Company> = mutableListOf()

    private val binding get() = _binding!!
    private val companyService: CompanyService by lazy {
        RetrofitClient.createRetrofit().create(CompanyService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainCompanyBinding.inflate(inflater, container, false)
        sharedPrefs = SharedPrefsManager(requireContext())

        binding.ivAddCompany.setOnClickListener {
            animateView(it)
            val intent = Intent(requireContext(), CompanyActivity::class.java)
            startActivity(intent)
        }

        setupRecyclerView()
        setupSearchView()
        fetchAllCompanies()

        return binding.root
    }

    private fun setupRecyclerView() {
        companyAdapter = CompanyAdapter(
            companyList,
            onEditClick = { company ->
                Toast.makeText(requireContext(), "Edit ${company.companyId}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { company ->
                Toast.makeText(requireContext(), "Delete ${company.companyId}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvCompany.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = companyAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchAllCompanies() {
        val token = sharedPrefs.authToken ?: return

        ApiHelper().callApi(
            context = requireContext(),
            call = companyService.getAllCompaniesByUserId("Bearer $token"),
            onSuccess = { response ->
                companyList.apply {
                    clear()
                    response?.let {
                        addAll(it)
                        allCompanies.clear()
                        allCompanies.addAll(it)
                    }
                }
                companyAdapter.notifyDataSetChanged()
            }
        )
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

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterCompanies(newText.orEmpty())
                return true
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterCompanies(query: String) {
        if (query.isEmpty()) {
            companyList.clear()
            companyList.addAll(allCompanies)
            companyAdapter.notifyDataSetChanged()
            binding.tvNoResults.visibility = View.GONE
            return
        }

        val filteredList = allCompanies.filter { company ->
            company.companyName?.contains(query, ignoreCase = true) == true ||
                    company.address?.contains(query, ignoreCase = true) == true ||
                    company.description?.contains(query, ignoreCase = true) == true
        }.toMutableList()

        companyList.clear()
        companyList.addAll(filteredList)
        companyAdapter.notifyDataSetChanged()

        binding.tvNoResults.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }
}