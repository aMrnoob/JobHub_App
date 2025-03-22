package com.example.jobhub.fragment

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.jobhub.databinding.ActivityMainBinding
import com.example.jobhub.databinding.MainHomeBinding

class HomeFragment : Fragment() {

    private var _binding: MainHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainHomeBinding.inflate(inflater, container, false)

        return binding.root
    }
}