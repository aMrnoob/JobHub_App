package com.example.befindingjob.repository;

import com.example.befindingjob.entity.Notification;
import com.example.befindingjob.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);
}
