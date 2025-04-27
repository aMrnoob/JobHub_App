package com.example.jobhub.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.jobhub.R
import com.example.jobhub.activity.LoginActivity
import com.example.jobhub.activity.ProfileActivity
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.MainProfileBinding
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.service.UserService

class ProfileFragment : Fragment() {
    private lateinit var binding: MainProfileBinding
    private lateinit var sharedPrefs: SharedPrefsManager

    private val userService: UserService by lazy { RetrofitClient.createRetrofit().create(UserService::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = SharedPrefsManager(requireContext())

        fetchUserProfile()

        binding.accountMenu.setOnClickListener {
            toggleSubMenu()
        }

        binding.updateProfile.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivityForResult(intent, UPDATE_PROFILE_REQUEST)
        }

        binding.deleteAccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete account")
                .setMessage("Are you sure to delete account? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ -> deleteAccount() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.termsOfService.setOnClickListener {
            val content = readTextFileFromRaw(R.raw.terms)
            showTextDialog("Terms of use", content)
        }

        binding.privacyPolicy.setOnClickListener {
            val content = readTextFileFromRaw(R.raw.privacy_policy)
            showTextDialog("Privacy policy", content)
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun fetchUserProfile() {
        val token = sharedPrefs.authToken ?: return

        ApiHelper().callApi(
            context = requireContext(),
            call = userService.getUser("Bearer $token"),
            onSuccess = { if (it != null) { updateUI(it) } }
        )
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(userInfo: UserDTO) {
        binding.userName.text = userInfo.fullName
        binding.welcomeText.text = "Hi, ${userInfo.fullName}"

        if (!userInfo.imageUrl.isNullOrEmpty()) {
            val bitmap = decodeBase64ToBitmap(userInfo.imageUrl.toString())
            if (bitmap != null) {
                binding.userAvatar.setImageBitmap(bitmap)
            } else {
                Log.e("ProfileFragment", "Lỗi giải mã ảnh")
            }
        }
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Lỗi giải mã Base64: ${e.message}")
            null
        }
    }

    private fun toggleSubMenu() {
        if (binding.accountSubMenu.visibility == View.GONE) {
            val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
            binding.accountSubMenu.startAnimation(slideUp)
            binding.accountSubMenu.visibility = View.VISIBLE

            slideUp.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) { binding.btnExpandAccountMenu.setImageResource(R.drawable.icon_arrow_down) }

                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {}
            })
        } else {
            val slideDown = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
            binding.accountSubMenu.startAnimation(slideDown)

            slideDown.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) { binding.btnExpandAccountMenu.setImageResource(R.drawable.icon_arrow_right) }

                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) { binding.accountSubMenu.visibility = View.GONE}
            })
        }
    }

    private fun readTextFileFromRaw(fileId: Int): String {
        return try {
            val inputStream = resources.openRawResource(fileId)
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Lỗi đọc file: ${e.message}")
            "Không thể đọc nội dung"
        }
    }

    private fun showTextDialog(title: String, message: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Đóng") { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()

        val textView = dialog.findViewById<TextView>(android.R.id.message)
        val scaleIn = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_in)
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val animationSet = AnimationSet(true)

        animationSet.addAnimation(scaleIn)
        animationSet.addAnimation(fadeIn)
        textView?.startAnimation(animationSet)
    }

    private fun logout() {
        sharedPrefs.clearAuthToken()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun deleteAccount() {
        val token = sharedPrefs.authToken ?: return

        ApiHelper().callApi(
            context = requireContext(),
            call = userService.deleteAccount("Bearer $token"),
            onSuccess = {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_PROFILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val username = data.getStringExtra("USERNAME")
            val avatarBase64 = data.getStringExtra("AVATAR")

            binding.userName.text = username

            if (!avatarBase64.isNullOrEmpty()) {
                val decodedString: ByteArray = Base64.decode(avatarBase64, Base64.DEFAULT)
                val decodedBitmap: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                binding.userAvatar.setImageBitmap(decodedBitmap)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchUserProfile()
    }

    companion object {
        private const val UPDATE_PROFILE_REQUEST = 1001
    }
}
