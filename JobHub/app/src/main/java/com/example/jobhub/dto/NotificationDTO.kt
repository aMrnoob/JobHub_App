package com.example.jobhub.dto

data class NotificationDTO (
    val senderId: Int,
    val receiverId: Int,
    val companyId: Int,
    val applicationId: Int,
    val content: String
)