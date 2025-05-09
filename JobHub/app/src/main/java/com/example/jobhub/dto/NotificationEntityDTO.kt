package com.example.jobhub.dto

data class NotificationEntityDTO(
    val id: Long? = 0,
    val content: String? = null,
    var read: Boolean,
    val createdAt: String? = null,
    val receiver: UserDTO? = null,
    val sender: UserDTO? = null,
    val application: ApplicationDTO? = null
)
