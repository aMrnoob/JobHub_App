package com.example.befindingjob.service;

import com.example.befindingjob.dto.NotificationDTO;
import com.example.befindingjob.dto.NotificationEntityDTO;
import com.example.befindingjob.entity.Notification;
import com.example.befindingjob.model.ApiResponse;

import java.util.List;

public interface NotificationService {
    ApiResponse<Void> createNotification(NotificationDTO notificationDTO);
    ApiResponse<List<NotificationEntityDTO>> getNotification(String token);
}
