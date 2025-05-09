package com.example.befindingjob.controller.admin;

import com.example.befindingjob.dto.MarkAsReadDTO;
import com.example.befindingjob.dto.NotificationDTO;
import com.example.befindingjob.dto.NotificationEntityDTO;
import com.example.befindingjob.entity.Notification;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/create-notification")
    public ApiResponse<Void> createNotification(@RequestBody NotificationDTO notificationDTO) {
        return notificationService.createNotification(notificationDTO);
    }

    @GetMapping("/get-all-notifications")
    public ApiResponse<List<NotificationEntityDTO>> getAllNotications(
            @RequestHeader("token") String token) {
        return notificationService.getNotification(token);
    }

    @PostMapping("/mark-as-read")
    public ApiResponse<Void> markAsRead(@RequestBody MarkAsReadDTO markAsReadDTO) {
        return notificationService.markAsRead(markAsReadDTO);
    }
}
