package com.example.befindingjob.service.impl;

import com.example.befindingjob.dto.employer.JobInfo;
import com.example.befindingjob.dto.jobseeker.SkillInfo;
import com.example.befindingjob.entity.Company;
import com.example.befindingjob.entity.Job;
import com.example.befindingjob.entity.Skill;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.repository.CompanyRepository;
import com.example.befindingjob.repository.JobRepository;
import com.example.befindingjob.repository.SkillRepository;
import com.example.befindingjob.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Override
    public ApiResponse<Void> createJob(JobInfo jobInfo) {
        Job job = new Job();

        Optional<Company> companyOpt = companyRepository.findById(jobInfo.getCompanyInfo().getCompanyId());

        if (companyOpt.isEmpty()) {
            return new ApiResponse<>(false, "Company not found!");
        }
        job.setCompany(companyOpt.get());

        job.setTitle(jobInfo.getJobName());
        job.setDescription(jobInfo.getDescription());
        job.setRequirements(jobInfo.getRequirements());
        job.setSalary(jobInfo.getSalary());
        job.setLocation(jobInfo.getLocation());
        job.setJobType(jobInfo.getJobType());
        job.setExperienceRequired(jobInfo.getExperienceRequired());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        job.setPostingDate(LocalDateTime.parse(jobInfo.getPostingDate(), formatter));
        job.setExpirationDate(LocalDateTime.parse(jobInfo.getExpirationDate(), formatter));

        Set<Skill> requiredSkills = new HashSet<>();
        for (SkillInfo skillInfo : jobInfo.getRequiredSkills()) {
            Skill skill = skillRepository.findBySkillName(skillInfo.getSkillName());

            if (skill == null) {
                skill = new Skill();
                skill.setSkillName(skillInfo.getSkillName());
                skill = skillRepository.save(skill);
            }

            requiredSkills.add(skill);
        }
        job.setRequiredSkills(requiredSkills);

        jobRepository.save(job);

        return new ApiResponse<>(true, "Job created successfully!");
    }
}
