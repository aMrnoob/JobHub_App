package com.example.jobhub.config

import android.content.Context
import android.content.SharedPreferences
import com.example.jobhub.dto.UserDTO
import com.google.gson.Gson

class SharedPrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("JobHubPrefs", Context.MODE_PRIVATE)

    var email: String?
        get() = prefs.getString("email", "")
        set(value) = prefs.edit().putString("email", value).apply()

    var password: String?
        get() = prefs.getString("password", "")
        set(value) = prefs.edit().putString("password", value).apply()

    var isRemembered: Boolean
        get() = prefs.getBoolean("isRemembered", false)
        set(value) = prefs.edit().putBoolean("isRemembered", value).apply()

    var authToken: String?
        get() = prefs.getString("authToken", null)
        set(value) = prefs.edit().putString("authToken", value).apply()

    fun clearRememberedCredentials() {
        prefs.edit()
            .remove("email")
            .remove("password")
            .remove("isRemembered")
            .apply()
    }

    var currentUserJson: String?
        get() = prefs.getString("currentUser", null)
        set(value) = prefs.edit().putString("currentUser", value).apply()

    fun saveCurrentUser(userJson: String) {
        currentUserJson = userJson
    }

    fun getCurrentUser(): UserDTO? {
        return currentUserJson?.let {
            Gson().fromJson(it, UserDTO::class.java)
        }
    }

    var currentJob: String?
        get() = prefs.getString("job", null)
        set(value) = prefs.edit().putString("job", value).apply()
}