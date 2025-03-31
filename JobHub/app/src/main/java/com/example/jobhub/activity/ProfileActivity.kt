package com.example.jobhub.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.databinding.MainProfileBinding
import com.example.jobhub.dto.UserDTO
import com.example.jobhub.service.UserService
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: MainProfileBinding
    private var userId: Int = -1
    private var selectedImageUri: Uri? = null

    private val userService: UserService by lazy {
        RetrofitClient.createRetrofit().create(UserService::class.java)
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.uploadedImageView.setImageURI(uri)
            binding.iconUploadImage.visibility = View.GONE
            binding.uploadedImageView.visibility = View.VISIBLE
        } else {
            Log.e("ProfileActivity", "Lỗi khi chọn ảnh")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("userId", -1)
        fetchUserProfile()

        binding.uploadImage.setOnClickListener { openImagePicker() }
        binding.uploadedImageView.setOnClickListener { openImagePicker() }

        binding.edtDateOfBirth.setOnClickListener { showDatePicker() }

        binding.btnUpdateProfile.setOnClickListener {
            if (validateFields()) {
                updateUserProfile()
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserProfile() {
        val sharedPreferences = getSharedPreferences("JobHubPrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("authToken", null)

        if (token.isNullOrBlank()) {
            Log.e("ProfileActivity", "Token không hợp lệ")
            return
        }

        ApiHelper().callApi(
            context = this,
            call = userService.getUser("Bearer $token"),
            onSuccess = { response ->
                response?.let { userDTO ->
                    userId = userDTO.userId ?: -1
                    updateUI(userDTO)
                }
            }
        )
    }

    private fun updateUI(userInfo: UserDTO) {
        binding.edtFullName.setText(userInfo.fullName)
        binding.edtEmail.setText(userInfo.email)
        binding.edtPhone.setText(userInfo.phone ?: "")
        binding.edtAddress.setText(userInfo.address ?: "")

        binding.edtDateOfBirth.setText(formatDateForDisplay(userInfo.dateOfBirth))

        if (!userInfo.imageUrl.isNullOrEmpty()) {
            val bitmap = decodeBase64ToBitmap(userInfo.imageUrl.toString())
            if (bitmap != null) {
                binding.uploadedImageView.setImageBitmap(bitmap)
                binding.iconUploadImage.visibility = View.GONE
                binding.uploadedImageView.visibility = View.VISIBLE
            }
        }
    }

    private fun updateUserProfile() {
        val sharedPreferences = getSharedPreferences("JobHubPrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("authToken", null)

        if (token.isNullOrBlank()) {
            Log.e("ProfileActivity", "Token không hợp lệ")
            return
        }

        val formattedDate = formatDateForBackend(binding.edtDateOfBirth.text.toString())

        val userInfo = UserDTO(
            userId = userId,
            fullName = binding.edtFullName.text.toString(),
            email = binding.edtEmail.text.toString(),
            phone = binding.edtPhone.text.toString(),
            imageUrl = encodeImageToBase64(),
            address = binding.edtAddress.text.toString(),
            dateOfBirth = formattedDate
        )

        ApiHelper().callApi(
            context = this,
            call = userService.updateUser(userInfo),
            onSuccess = {
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun validateFields(): Boolean {
        val fields = listOf(
            binding.edtFullName.text.toString(),
            binding.edtEmail.text.toString(),
            binding.edtPhone.text.toString(),
            binding.edtAddress.text.toString(),
            binding.edtDateOfBirth.text.toString()
        )
        return fields.all { it.isNotBlank() } && (selectedImageUri != null || binding.uploadedImageView.drawable != null)
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun encodeImageToBase64(): String? {
        val bitmap = (binding.uploadedImageView.drawable as? BitmapDrawable)?.bitmap
        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
        return null
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Lỗi khi giải mã ảnh Base64: ${e.message}")
            null
        }
    }

    private fun formatDateForBackend(inputDate: String): String {
        return inputDate
    }

    private fun formatDateForDisplay(apiDate: String): String {
        return try {
            val inputFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            )

            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            var date: Date? = null
            for (format in inputFormats) {
                try {
                    date = format.parse(apiDate)
                    if (date != null) break
                } catch (_: Exception) {}
            }

            if (date != null) outputFormat.format(date) else apiDate
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Lỗi định dạng ngày từ API: ${e.message}")
            apiDate
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val currentText = binding.edtDateOfBirth.text.toString()
        if (currentText.isNotEmpty()) {
            val dateParts = currentText.split("/")
            if (dateParts.size == 3) {
                calendar.set(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
            }
        }

        val datePicker = DatePickerDialog(
            this, { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                binding.edtDateOfBirth.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }
}
