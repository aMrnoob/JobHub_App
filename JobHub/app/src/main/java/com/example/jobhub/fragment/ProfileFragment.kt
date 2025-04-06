package com.example.jobhub.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jobhub.R
import com.example.jobhub.activity.LoginActivity
import com.example.jobhub.activity.ProfileActivity
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainProfileBinding
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.model.ApiResponse
import com.example.jobhub.service.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    private lateinit var binding: MainProfileBinding
    private var userId: Int = -1
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)

        fetchUserProfile()

        binding.accountMenu.setOnClickListener {
            toggleSubMenu()
        }

        binding.updateProfile.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            intent.putExtra("userId", userId)
            startActivityForResult(intent, UPDATE_PROFILE_REQUEST)
        }

        binding.deleteAccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản không? Hành động này không thể hoàn tác!")
                .setPositiveButton("Xóa") { _, _ -> deleteAccount() }
                .setNegativeButton("Hủy", null)
                .show()
        }

        binding.termsOfService.setOnClickListener {
            val content = readTextFileFromRaw(R.raw.terms)
            showTextDialog("Điều khoản sử dụng", content)
        }

        binding.privacyPolicy.setOnClickListener {
            val content = readTextFileFromRaw(R.raw.privacy_policy)
            showTextDialog("Chính sách bảo mật", content)
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val token = sharedPreferences.getString("authToken", null)

        if (token.isNullOrBlank()) {
            Log.e("ProfileFragment", "Token không hợp lệ")
            return
        }

        val apiService = RetrofitClient.createRetrofit().create(UserService::class.java)
        apiService.getUser("Bearer $token").enqueue(object :
            Callback<ApiResponse<UserDTO>> {
            override fun onResponse(call: Call<ApiResponse<UserDTO>>, response: Response<ApiResponse<UserDTO>>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { userInfo ->
                        userId = userInfo.userId ?: -1
                        updateUI(userInfo)
                    }
                } else {
                    Log.e("ProfileFragment", "Lỗi khi lấy thông tin user: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<UserDTO>>, t: Throwable) {
                Log.e("ProfileFragment", "API call thất bại: ${t.message}")
            }
        })
    }

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
            binding.accountSubMenu.visibility = View.VISIBLE
            binding.btnExpandAccountMenu.setImageResource(R.drawable.icon_arrow_up)
        } else {
            binding.accountSubMenu.visibility = View.GONE
            binding.btnExpandAccountMenu.setImageResource(R.drawable.icon_arrow_down)
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
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Đóng") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun logout() {
        val editor = sharedPreferences.edit()
        editor.remove("authToken")
        editor.apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun deleteAccount() {
        val token = sharedPreferences.getString("authToken", null) ?: return

        val apiService = RetrofitClient.createRetrofit().create(UserService::class.java)
        apiService.deleteAccount("Bearer $token").enqueue(object : Callback<ApiResponse<UserDTO>> {
            override fun onResponse(call: Call<ApiResponse<UserDTO>>, response: Response<ApiResponse<UserDTO>>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(requireContext(), "Xóa tài khoản thành công!", Toast.LENGTH_SHORT).show()
                    logout()
                } else {
                    Toast.makeText(requireContext(), "Xóa tài khoản thất bại!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<UserDTO>>, t: Throwable) {
                Toast.makeText(requireContext(), "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

    companion object {
        private const val UPDATE_PROFILE_REQUEST = 1001
    }
}
