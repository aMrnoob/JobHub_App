package com.example.jobhub.service

import com.example.jobhub.dto.MarkAsReadDTO
import com.example.jobhub.dto.NotificationDTO
import com.example.jobhub.dto.NotificationEntityDTO
import com.example.jobhub.entity.Notification
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface NotificationService {

    @POST("api/notifications/create-notification")
    fun createNotification(@Body notificationDTO: NotificationDTO): Call<ApiResponse<Void>>

    @GET("api/notifications/get-all-notifications")
    fun getAllNotifications(
        @Header("token") token: String
    ): Call<ApiResponse<List<NotificationEntityDTO>>>

    @POST("api/notifications/mark-as-read")
    fun markAsRead(@Body markAsReadDTO: MarkAsReadDTO): Call<ApiResponse<Void>>
}