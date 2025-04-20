package com.example.befindingjob.dto;

import com.example.befindingjob.entity.Job;
import com.example.befindingjob.entity.enumm.JobType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Getter
@Setter
@AllArgsConstructor
public class ItemJobDTO {
    private Integer jobId;
    private String title;
    private String description;
    private String requirements;
    private String salary;
    private String location;
    private JobType jobType;
    private String experienceRequired;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationDate;
    private ItemCompanyDTO company;
    private List<SkillDTO> requiredSkills;
    private List<ApplicationDTO> applications;

    public ItemJobDTO(Job job) {
        this.jobId = job.getJobId();
        this.title = job.getTitle();
        this.description = job.getDescription();
        this.requirements = job.getRequirements();
        this.salary = job.getSalary();
        this.location = job.getLocation();
        this.jobType = job.getJobType();
        this.experienceRequired = job.getExperienceRequired();
        this.expirationDate = job.getExpirationDate();
        this.company = new ItemCompanyDTO(job.getCompany());
        this.requiredSkills = job.getRequiredSkills().stream()
                .map(SkillDTO::new)
                .collect(Collectors.toList());
        this.applications = job.getApplications().stream().map(ApplicationDTO::new).collect(Collectors.toList());
    }
}
