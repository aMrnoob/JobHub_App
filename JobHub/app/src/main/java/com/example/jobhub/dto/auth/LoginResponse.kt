package com.example.jobhub.dto.auth

import android.os.Parcelable
import com.example.jobhub.entity.enumm.Role
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginResponse(
    val token: String,
    val userId: Int,
    val role: Role,
    val fullName: String
) : Parcelable
