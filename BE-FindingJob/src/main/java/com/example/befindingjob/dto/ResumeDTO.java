package com.example.befindingjob.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResumeDTO {
    private Integer resumeId;
    private Integer applicationId;
    private String resumeUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

