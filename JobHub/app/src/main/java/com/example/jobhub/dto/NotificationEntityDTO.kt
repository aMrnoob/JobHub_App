package com.example.jobhub.dto

data class NotificationEntityDTO(
    val id: Long? = null,
    val content: String? = null,
    val isRead: Boolean = false,
    val createdAt: String? = null,
    val receiver: UserDTO? = null,
    val sender: UserDTO? = null,
    val application: ApplicationDTO? = null
)
