package com.example.jobhub.config

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.entity.enumm.Role
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SharedPrefsManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "JobHubPrefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var userId: Int?
        get() = prefs.getInt("userId", -1).takeIf { it != -1 }
        set(value) = prefs.edit().putInt("userId", value ?: -1).apply()

    var role: Role?
        get() = prefs.getString("role", null)?.let { Role.valueOf(it) }
        set(value) = prefs.edit().putString("role", value?.name).apply()

    var fullName: String?
        get() = prefs.getString("full_name", null)
        set(value) = prefs.edit().putString("full_name", value).apply()

    var email: String?
        get() = prefs.getString("email", "")
        set(value) = prefs.edit().putString("email", value).apply()

    var password: String?
        get() = prefs.getString("password", "")
        set(value) = prefs.edit().putString("password", value).apply()

    var isRemembered: Boolean
        get() = prefs.getBoolean("isRemembered", false)
        set(value) = prefs.edit().putBoolean("isRemembered", value).apply()

    fun clearRemembered() {
        prefs.edit()
            .remove("email")
            .remove("password")
            .remove("isRemembered")
            .apply()
    }

    var authToken: String?
        get() = prefs.getString("authToken", null)
        set(value) = prefs.edit().putString("authToken", value).apply()

    fun clearAuthToken() {
        prefs.edit().remove("authToken").apply()
    }

    private var currentJob: String?
        get() = prefs.getString("job", null)
        set(value) = prefs.edit().putString("job", value).apply()

    fun saveCurrentJob(itemJobDTO: ItemJobDTO) {
        val jobJson = Gson().toJson(itemJobDTO)
        currentJob = jobJson
    }

    fun getCurrentJob(): ItemJobDTO? {
        return currentJob?.let {
            Gson().fromJson(it, ItemJobDTO::class.java)
        }
    }
}