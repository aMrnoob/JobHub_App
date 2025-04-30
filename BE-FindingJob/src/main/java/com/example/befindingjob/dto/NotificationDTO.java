package com.example.befindingjob.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private int senderId;
    private int companyId;
    private int applicationId;
    private String content;
}
