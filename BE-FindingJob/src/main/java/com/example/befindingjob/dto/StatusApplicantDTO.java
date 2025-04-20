package com.example.befindingjob.dto;

import com.example.befindingjob.entity.enumm.ApplicationStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class StatusApplicantDTO {
    private int applicationId;
    private ApplicationStatus status;
    private String message;
    private LocalDateTime interviewDate;
}
