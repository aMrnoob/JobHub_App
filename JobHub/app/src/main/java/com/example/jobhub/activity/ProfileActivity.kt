package com.example.jobhub.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainEditprofileBinding
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.model.ApiResponse
import com.example.jobhub.service.UserService
import com.example.jobhub.entity.enumm.Role
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: MainEditprofileBinding
    private var userId: Int = -1
    private var selectedImageUri: Uri? = null
    private var userRole: Role = Role.UNDEFINED

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.uploadedImageView.setImageURI(it)
            binding.iconUploadImage.visibility = View.GONE
            binding.uploadedImageView.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        fetchUserProfile()
    }

    private fun setupViews() {
        binding.uploadImage.setOnClickListener { pickImage() }
        binding.uploadedImageView.setOnClickListener { pickImage() }
        binding.edtDateOfBirth.setOnClickListener { showDatePicker() }

        binding.btnUpdateProfile.setOnClickListener {
            if (validateFields()) updateUserProfile()
        }
    }

    private fun pickImage() {
        imagePicker.launch("image/*")
    }

    private fun fetchUserProfile() {
        val token = getSharedPreferences("JobHubPrefs", MODE_PRIVATE).getString("authToken", null)
        if (token.isNullOrBlank()) return

        RetrofitClient.createRetrofit().create(UserService::class.java)
            .getUser("Bearer $token")
            .enqueue(object : Callback<ApiResponse<UserDTO>> {
                override fun onResponse(call: Call<ApiResponse<UserDTO>>, response: Response<ApiResponse<UserDTO>>) {
                    response.body()?.data?.let { updateUI(it) }
                }

                override fun onFailure(call: Call<ApiResponse<UserDTO>>, t: Throwable) {
                    Log.e("ProfileActivity", "Lỗi API: ${t.message}")
                }
            })
    }

    private fun updateUI(user: UserDTO) {
        userId = user.userId ?: -1
        userRole = user.role
        binding.edtFullName.setText(user.fullName ?: "")
        binding.edtEmail.setText(user.email ?: "")
        binding.edtPhone.setText(user.phone ?: "")
        binding.edtAddress.setText(user.address ?: "")
        binding.edtDateOfBirth.setText(formatDateForDisplay(user.dateOfBirth ?: ""))

        user.imageUrl?.let {
            decodeBase64ToBitmap(it)?.let { bitmap ->
                binding.uploadedImageView.setImageBitmap(bitmap)
                binding.iconUploadImage.visibility = View.GONE
                binding.uploadedImageView.visibility = View.VISIBLE
            }
        }
    }

    private fun updateUserProfile() {
        val token = getSharedPreferences("JobHubPrefs", MODE_PRIVATE).getString("authToken", null)
        if (token.isNullOrBlank()) return

        val user = UserDTO(
            userId = userId,
            fullName = binding.edtFullName.text.toString(),
            email = binding.edtEmail.text.toString(),
            phone = binding.edtPhone.text.toString(),
            imageUrl = encodeImageToBase64(),
            address = binding.edtAddress.text.toString(),
            dateOfBirth = formatDateForBackend(binding.edtDateOfBirth.text.toString()),
            role = userRole
        )

        RetrofitClient.createRetrofit().create(UserService::class.java)
            .updateUser(user)
            .enqueue(object : Callback<ApiResponse<Void>> {
                override fun onResponse(call: Call<ApiResponse<Void>>, response: Response<ApiResponse<Void>>) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        Toast.makeText(this@ProfileActivity, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()

                        val resultIntent = Intent()
                        resultIntent.putExtra("USERNAME", user.fullName)
                        resultIntent.putExtra("PROFILE_UPDATED", true)
                        setResult(RESULT_OK, resultIntent)

                        finish()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileActivity", "Lỗi cập nhật: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Lỗi kết nối!", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileActivity", "API thất bại: ${t.message}")
                }
            })
    }

    private fun encodeImageToBase64(): String? {
        val bitmap = (binding.uploadedImageView.drawable as? BitmapDrawable)?.bitmap ?: return null

        val resizedBitmap = resizeImage(bitmap, 512, 512)

        return ByteArrayOutputStream().apply {
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, this)
        }.toByteArray().let {
            Base64.encodeToString(it, Base64.DEFAULT)
        }
    }

    private fun resizeImage(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = image.width
        val height = image.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        var newWidth = maxWidth
        var newHeight = (newWidth / ratioBitmap).toInt()

        if (newHeight > maxHeight) {
            newHeight = maxHeight
            newWidth = (newHeight * ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(image, newWidth, newHeight, true)
    }

    private fun decodeBase64ToBitmap(base64: String) = try {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        Log.e("ProfileActivity", "Giải mã ảnh lỗi: ${e.message}")
        null
    }

    private fun formatDateForDisplay(apiDate: String): String {
        val formats = listOf("yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd")
        formats.forEach {
            try {
                val sdf = SimpleDateFormat(it, Locale.getDefault())
                val date = sdf.parse(apiDate)
                if (date != null) {
                    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                }
            } catch (_: Exception) {}
        }
        return apiDate
    }

    private fun formatDateForBackend(date: String): String = date

    private fun validateFields(): Boolean {
        val required = listOf(
            binding.edtFullName.text,
            binding.edtEmail.text,
            binding.edtPhone.text,
            binding.edtAddress.text,
            binding.edtDateOfBirth.text
        )
        return required.all { it.isNotBlank() } &&
                (selectedImageUri != null || binding.uploadedImageView.drawable != null)
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        binding.edtDateOfBirth.text.toString().takeIf { it.isNotEmpty() }?.let {
            val parts = it.split("/")
            if (parts.size == 3) cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->
                binding.edtDateOfBirth.setText(String.format("%02d/%02d/%04d", day, month + 1, year))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            show()
        }
    }
}