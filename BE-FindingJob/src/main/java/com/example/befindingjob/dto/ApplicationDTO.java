package com.example.befindingjob.dto;

import com.example.befindingjob.entity.Application;
import com.example.befindingjob.entity.enumm.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDTO {
    private Integer applicationId;
    private JobDTO jobDTO;
    private UserDTO userDTO;
    private String coverLetter;
    private String resumeUrl;
    private ApplicationStatus status;
    private LocalDateTime applicationDate;

    public ApplicationDTO(Integer applicationId, ItemJobDTO itemJobDTO, UserDTO userDTO, String coverLetter, ApplicationStatus status, LocalDateTime applicationDate) {
        this.applicationId = applicationId;
        this.jobDTO = new JobDTO(itemJobDTO);
        this.userDTO = userDTO;
        this.coverLetter = coverLetter;
        this.status = status;
        this.applicationDate = applicationDate;
    }

    public ApplicationDTO(Application application) {
        this.applicationId = application.getApplicationId();
        this.jobDTO = null;
        this.userDTO = new UserDTO(application.getUser());
        this.coverLetter = application.getCoverLetter();
        this.status = application.getStatus();
        this.applicationDate = application.getApplicationDate();
        this.resumeUrl = application.getResume() != null ? application.getResume().getResumeUrl() : null;
    }
}
