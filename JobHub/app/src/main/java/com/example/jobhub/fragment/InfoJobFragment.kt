package com.example.jobhub.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.FragmentJobDetailBinding
import com.example.jobhub.dto.ItemJobDTO

class InfoJobFragment : Fragment() {

    private lateinit var sharedPrefs: SharedPrefsManager

    private var _binding: FragmentJobDetailBinding? = null
    private var jobDTO: ItemJobDTO? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = SharedPrefsManager(requireContext())
        jobDTO = sharedPrefs.getCurrentJob()

        showInfoJob()
    }

    @SuppressLint("SetTextI18n")
    private fun showInfoJob() {
        val requirementsText = "•  " + jobDTO?.requirements?.split(",")?.joinToString(separator = "\n•  ") { it.trim() }

        binding.tvDescription.text = ("-   " + jobDTO?.description?.replace(".", ".\n-   "))
        binding.tvRequirement.text = requirementsText
    }
}