package com.example.befindingjob.dto;

import com.example.befindingjob.entity.enumm.JobType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobDTO {
    private Integer jobId;
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

    public JobDTO(ItemJobDTO itemJobDTO) {
        this.jobId = itemJobDTO.getJobId();
        this.title = itemJobDTO.getTitle();
        this.description = itemJobDTO.getDescription();
        this.requirements = itemJobDTO.getRequirements();
        this.salary = itemJobDTO.getSalary();
        this.location = itemJobDTO.getLocation();
        this.jobType = itemJobDTO.getJobType();
        this.experienceRequired = itemJobDTO.getExperienceRequired();
        this.expirationDate = (itemJobDTO.getExpirationDate() != null) ? itemJobDTO.getExpirationDate().toString() : null;
        this.companyName = (itemJobDTO.getCompany() != null) ? itemJobDTO.getCompany().getCompanyName() : null;
        this.requiredSkills = itemJobDTO.getRequiredSkills() != null
                ? itemJobDTO.getRequiredSkills().stream().collect(Collectors.toSet())
                : null;
    }
}
