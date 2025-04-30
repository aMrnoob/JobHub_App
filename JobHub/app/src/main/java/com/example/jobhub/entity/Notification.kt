package com.example.jobhub.entity

data class Notification(
    val id: Long,
    val content: String,
    val isRead: Boolean,
    val createdAt: String,
    val receiver: User?,
    val sender: User?,
    val application: Application?
)
