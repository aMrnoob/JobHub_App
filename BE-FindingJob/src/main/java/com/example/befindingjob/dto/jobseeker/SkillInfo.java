package com.example.befindingjob.dto.jobseeker;

import com.example.befindingjob.dto.admin.UserInfo;
import com.example.befindingjob.dto.employer.JobInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillInfo {
    private int skillId;
    private String skillName;
    private Set<UserInfo> users;
    private Set<JobInfo> jobs;
}
