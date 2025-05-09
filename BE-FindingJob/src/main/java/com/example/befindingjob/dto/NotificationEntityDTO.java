package com.example.befindingjob.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEntityDTO {
    private Long id;
    private String content;
    private boolean read;
    private String createdAt;
    private UserDTO receiver;
    private UserDTO sender;
    private ApplicationDTO application;
}
