package com.example.jobhub.activity

import android.content.Intent
import android.os.Bundle
import com.example.jobhub.anim.AnimationHelper
import com.example.jobhub.databinding.WelcomeScreenBinding

class WelcomeActivity : BaseActivity() {

    private lateinit var binding: WelcomeScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = WelcomeScreenBinding.inflate((layoutInflater))
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            AnimationHelper.animateScale(it)
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            AnimationHelper.animateScale(it)
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}