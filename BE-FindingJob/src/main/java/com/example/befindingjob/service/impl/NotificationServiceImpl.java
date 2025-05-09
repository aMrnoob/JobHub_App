package com.example.befindingjob.service.impl;

import com.example.befindingjob.dto.*;
import com.example.befindingjob.entity.Application;
import com.example.befindingjob.entity.Company;
import com.example.befindingjob.entity.Notification;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.repository.ApplicationRepository;
import com.example.befindingjob.repository.CompanyRepository;
import com.example.befindingjob.repository.NotificationRepository;
import com.example.befindingjob.repository.UserRepository;
import com.example.befindingjob.service.JwtService;
import com.example.befindingjob.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JwtService jwtService;

    @Override
    public ApiResponse<Void> createNotification(NotificationDTO notificationDTO) {
        try {
            Notification notification = new Notification();

            Application application = applicationRepository.findById(notificationDTO.getApplicationId())
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            User sender;
            User receiver;

            if(notificationDTO.getReceiverId() == 0) {
                sender = userRepository.findById(notificationDTO.getSenderId())
                        .orElseThrow(() -> new RuntimeException("Sender not found"));

                Company company = companyRepository.findById(notificationDTO.getCompanyId())
                        .orElseThrow(() -> new RuntimeException("Company not found"));

                receiver = company.getUser();
            } else {
                sender = userRepository.findById(notificationDTO.getSenderId())
                        .orElseThrow(() -> new RuntimeException("Sender not found"));

                receiver = userRepository.findById(notificationDTO.getReceiverId())
                        .orElseThrow(() -> new RuntimeException("Sender not found"));

            }

            notification.setSender(sender);
            notification.setReceiver(receiver);
            notification.setRead(false);
            notification.setApplication(application);
            notification.setContent(notificationDTO.getContent());
            notification.setCreatedAt(LocalDateTime.now());

            notificationRepository.save(notification);

            return new ApiResponse<>(true, "", null);
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
            e.printStackTrace();
            return new ApiResponse<>(false, "", null);
        }
    }

    @Override
    public ApiResponse<List<NotificationEntityDTO>> getNotification(String token) {
        try {
            if (!jwtService.isValidToken(token)) {
                return new ApiResponse<>(false, "", null);
            }
            int userId = jwtService.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Notification> notifications = notificationRepository.findByReceiverOrderByCreatedAtDesc(user);

            List<NotificationEntityDTO> notificationDTOs = notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return new ApiResponse<>(true, "", notificationDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "", null);
        }
    }

    @Override
    public ApiResponse<Void> markAsRead(MarkAsReadDTO markAsReadDTO) {
        try {
            if (markAsReadDTO == null) {
                return new ApiResponse<>(false, "Request data cannot be null", null);
            }

            String token = markAsReadDTO.getToken();
            Long notificationId = markAsReadDTO.getNotificationId();

            if (notificationId == null) {
                return new ApiResponse<>(false, "", null);
            }

            if (!jwtService.isValidToken(token)) {
                return new ApiResponse<>(false, "", null);
            }

            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found"));

            notification.setRead(true);
            notificationRepository.save(notification);

            return new ApiResponse<>(true, "", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Error: " + e.getMessage(), null);
        }
    }

    private NotificationEntityDTO convertToDTO(Notification notification) {
        NotificationEntityDTO dto = new NotificationEntityDTO();
        dto.setId(notification.getId());
        dto.setContent(notification.getContent());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt().toString());

        if (notification.getReceiver() != null) {
            dto.setReceiver(new UserDTO(notification.getReceiver()));
        }

        if (notification.getSender() != null) {
            dto.setSender(new UserDTO(notification.getSender()));
        }

        if (notification.getApplication() != null) {
            dto.setApplication(new ApplicationDTO(notification.getApplication()));
        }

        return dto;
    }
}
