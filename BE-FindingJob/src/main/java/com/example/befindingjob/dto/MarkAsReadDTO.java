package com.example.befindingjob.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarkAsReadDTO {
    private String token;
    private Long notificationId;
}
