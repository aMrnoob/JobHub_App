package com.example.jobhub.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.FragmentCompanyBinding
import com.example.jobhub.dto.ItemJobDTO

class InfoCompanyFragment : Fragment() {

    private lateinit var sharedPrefs: SharedPrefsManager

    private var _binding: FragmentCompanyBinding? = null
    private var jobDTO: ItemJobDTO? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompanyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = SharedPrefsManager(requireContext())
        jobDTO = sharedPrefs.getCurrentJob()

        showCompanyInfo()
    }

    private fun showCompanyInfo() {
        binding.tvCompanyName.text = jobDTO?.company?.companyName ?: ""
        binding.tvDescription.text = jobDTO?.company?.description ?: ""
    }
}