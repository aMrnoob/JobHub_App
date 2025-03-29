package com.example.befindingjob.dto.employer;

import com.example.befindingjob.dto.jobseeker.SkillInfo;
import com.example.befindingjob.entity.enumm.JobType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobInfo {
    private int jobId;
    private String title;
    private CompanyInfo companyInfo;
    private String description;
    private String requirements;
    private String salary;
    private String location;
    private JobType jobType;
    private String experienceRequired;
    private String postingDate;
    private String expirationDate;
    private Set<SkillInfo> requiredSkills;
}
