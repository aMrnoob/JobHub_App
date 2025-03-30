package com.example.befindingjob.dto;

import com.example.befindingjob.entity.enumm.JobType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Data
@Getter
@Setter
@AllArgsConstructor
public class JobDTO {
    private String jobId;
    private String title;
    private String description;
    private String requirements;
    private String salary;
    private String location;
    private JobType jobType;
    private String experienceRequired;
    private String postingDate;
    private String expirationDate;
    private String companyName;
    private Set<SkillDTO> requiredSkills;
}
