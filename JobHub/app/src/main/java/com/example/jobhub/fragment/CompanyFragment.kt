package com.example.jobhub.fragment

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.R
import com.example.jobhub.activity.CompanyActivity
import com.example.jobhub.adapter.CompanyAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.MainCompanyBinding
import com.example.jobhub.entity.Company
import com.example.jobhub.entity.enumm.CompanyAction
import com.example.jobhub.service.CompanyService

class CompanyFragment : Fragment() {

    private lateinit var companyAdapter: CompanyAdapter
    private lateinit var sharedPrefs: SharedPrefsManager

    private var _binding: MainCompanyBinding? = null
    private var allCompanies: MutableList<Company> = mutableListOf()
    private var companyList: MutableList<Company> = mutableListOf()
    private var isFragmentVisible = false

    private val binding get() = _binding!!
    private val companyService: CompanyService by lazy { RetrofitClient.createRetrofit().create(CompanyService::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainCompanyBinding.inflate(inflater, container, false)
        sharedPrefs = SharedPrefsManager(requireContext())
        refreshHandler.post(refreshRunnable)

        binding.ivAddCompany.setOnClickListener {
            animateView(it)
            startActivity(Intent(requireContext(), CompanyActivity::class.java))
        }

        setupRecyclerView()
        setupSearchView()
        fetchAllCompanies()

        return binding.root
    }

    private fun setupRecyclerView() {
        companyAdapter = CompanyAdapter(
            companyList,
            onActionClick = { company, action ->
                when (action) {
                    CompanyAction.CLICK -> { }

                    CompanyAction.EDIT -> {
                        val intent = Intent(requireContext(), CompanyActivity::class.java)
                        intent.putExtra("company", company)
                        startActivity(intent)
                    }

                    CompanyAction.DELETE -> { company.companyId?.let { deleteCompany(it) } }
                }
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
                response?.let {
                    val sortedList = it.sortedByDescending { company -> company.companyId }
                    allCompanies.clear()
                    allCompanies.addAll(sortedList)
                }

                if (binding.searchView.query.isNullOrEmpty()) {
                    companyList.clear()
                    companyList.addAll(allCompanies)
                }
                companyAdapter.notifyDataSetChanged()
            }
        )
    }

    private fun deleteCompany(companyId: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)

        val btnYes = dialogView.findViewById<TextView>(R.id.btnYes)
        val btnNo = dialogView.findViewById<TextView>(R.id.btnNo)
        val alertDialog = dialogBuilder.create()

        btnYes.setOnClickListener {
            ApiHelper().callApi(
                context = requireContext(),
                call = companyService.deleteCompany(companyId),
                onSuccess = { alertDialog.dismiss() }
            )
        }

        btnNo.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
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

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (isFragmentVisible && binding.searchView.query.isNullOrEmpty()) { fetchAllCompanies() }
            refreshHandler.postDelayed(this, 60000)
        }
    }

    override fun onResume() {
        super.onResume()
        isFragmentVisible = true
        refreshHandler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        isFragmentVisible = false
        refreshHandler.removeCallbacks(refreshRunnable)
    }
}