package com.example.jobhub.config

import android.content.Context
import android.content.SharedPreferences

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
}