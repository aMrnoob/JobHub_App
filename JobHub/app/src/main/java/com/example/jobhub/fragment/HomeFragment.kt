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

        setupAnimation()

        return binding.root
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

    private fun setupAnimation() {
        listOf(
            binding.ivNotification, binding.ivSearch, binding.ivMenu,
            binding.tvTips, binding.tvViewMore1, binding.tvAllJob,
            binding.tvWriter, binding.tvDesign, binding.tvHR, binding.tvViewMore,
            binding.tvProgramer, binding.tvFinance, binding.tvCustomerService,
            binding.tvFoodRestaurant, binding.tvMusicProducer
        ).forEach { it.setOnClickListener { animateView(it) } }
    }
}