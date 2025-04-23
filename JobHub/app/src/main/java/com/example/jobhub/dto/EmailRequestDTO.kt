package com.example.jobhub.dto

data class EmailRequestDTO(
    val recipient: String,
    val subject: String,
    val body: String,
    val isHtml: Boolean = false
)