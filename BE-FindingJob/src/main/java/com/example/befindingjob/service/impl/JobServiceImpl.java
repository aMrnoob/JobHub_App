package com.example.befindingjob.service.impl;

import com.example.befindingjob.dto.employer.CompanyInfo;
import com.example.befindingjob.dto.employer.JobInfo;
import com.example.befindingjob.dto.jobseeker.SkillInfo;
import com.example.befindingjob.entity.Company;
import com.example.befindingjob.entity.Job;
import com.example.befindingjob.entity.Skill;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.repository.CompanyRepository;
import com.example.befindingjob.repository.JobRepository;
import com.example.befindingjob.repository.SkillRepository;
import com.example.befindingjob.repository.UserRepository;
import com.example.befindingjob.service.JobService;
import com.example.befindingjob.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Override
    public ApiResponse<Void> createJob(JobInfo jobInfo) {
        Job job = new Job();

        Optional<Company> companyOpt = companyRepository.findById(jobInfo.getCompanyInfo().getCompanyId());

        if (companyOpt.isEmpty()) {
            return new ApiResponse<>(false, "Company not found!");
        }
        job.setCompany(companyOpt.get());

        job.setTitle(jobInfo.getTitle());
        job.setDescription(jobInfo.getDescription());
        job.setRequirements(jobInfo.getRequirements());
        job.setSalary(jobInfo.getSalary());
        job.setLocation(jobInfo.getLocation());
        job.setJobType(jobInfo.getJobType());
        job.setExperienceRequired(jobInfo.getExperienceRequired());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

        job.setPostingDate(LocalDate.parse(jobInfo.getPostingDate(), formatter).atStartOfDay());
        job.setExpirationDate(LocalDate.parse(jobInfo.getExpirationDate(), formatter).atStartOfDay());


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

    @Override
    public ApiResponse<Void> updateJob(JobInfo jobInfo) {
        if (jobInfo == null) {
            return new ApiResponse<>(false, "Invalid job data.");
        }

        Optional<Job> jobOpt = jobRepository.findById(jobInfo.getJobId());
        if (jobOpt.isEmpty()) {
            return new ApiResponse<>(false, "Job not found.");
        }

        Job job = jobOpt.get();

        Optional<Company> companyOpt = companyRepository.findById(jobInfo.getCompanyInfo().getCompanyId());
        if (companyOpt.isEmpty()) {
            return new ApiResponse<>(false, "Company not found.");
        }
        job.setCompany(companyOpt.get());

        job.setTitle(jobInfo.getTitle());
        job.setDescription(jobInfo.getDescription());
        job.setRequirements(jobInfo.getRequirements());
        job.setSalary(jobInfo.getSalary());
        job.setLocation(jobInfo.getLocation());
        job.setJobType(jobInfo.getJobType());
        job.setExperienceRequired(jobInfo.getExperienceRequired());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            if (jobInfo.getPostingDate() != null && !jobInfo.getPostingDate().isEmpty()) {
                job.setPostingDate(LocalDate.parse(jobInfo.getPostingDate(), formatter).atStartOfDay());
            }
            if (jobInfo.getExpirationDate() != null && !jobInfo.getExpirationDate().isEmpty()) {
                job.setExpirationDate(LocalDate.parse(jobInfo.getExpirationDate(), formatter).atStartOfDay());
            }
        } catch (Exception e) {
            return new ApiResponse<>(false, "Invalid date format. Expected format: d/M/yyyy.");
        }

        Set<Skill> updatedSkills = new HashSet<>();
        for (SkillInfo skillInfo : jobInfo.getRequiredSkills()) {
            Skill skill = skillRepository.findBySkillName(skillInfo.getSkillName());
            if (skill == null) {
                skill = new Skill();
                skill.setSkillName(skillInfo.getSkillName());
                skill = skillRepository.save(skill);
            }
            updatedSkills.add(skill);
        }
        job.setRequiredSkills(updatedSkills);

        jobRepository.save(job);

        return new ApiResponse<>(true, "Job updated successfully.");
    }

    @Override
    public ApiResponse<List<JobInfo>> getAllJobsByUser(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return new ApiResponse<>(false, "Invalid token format. Token must start with 'Bearer '.");
        }

        token = token.substring(7);

        if (!jwtService.isTokenValid(token)) {
            return new ApiResponse<>(false, "Invalid or expired token.");
        }

        Integer userId = jwtService.extractUserId(token);
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return new ApiResponse<>(false, "User not found!", null);
        }

        User user = userOpt.get();
        Set<Company> companies = user.getCompanies();

        List<JobInfo> jobInfos = new ArrayList<>();

        for (Company company : companies) {
            List<Job> jobs = jobRepository.findByCompany(company);
            for (Job job : jobs) {
                CompanyInfo companyInfo = new CompanyInfo(
                        company.getCompanyId(),
                        company.getCompanyName(),
                        null,
                        company.getAddress(),
                        company.getLogoUrl(),
                        company.getWebsite(),
                        company.getDescription(),
                        null,
                        null
                );

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                String postingDateStr = (job.getPostingDate() != null) ? job.getPostingDate().format(formatter) : null;
                String expirationDateStr = (job.getExpirationDate() != null) ? job.getExpirationDate().format(formatter) : null;

                JobInfo jobInfo = new JobInfo(
                        job.getJobId(),
                        job.getTitle(),
                        companyInfo,
                        job.getDescription(),
                        job.getRequirements(),
                        job.getSalary(),
                        job.getLocation(),
                        job.getJobType(),
                        job.getExperienceRequired(),
                        postingDateStr,
                        expirationDateStr,
                        job.getRequiredSkills().stream()
                                .map(skill -> new SkillInfo(skill.getSkillId(), skill.getSkillName(), null, null))
                                .collect(Collectors.toSet())
                );
                jobInfos.add(jobInfo);
            }
        }

        return new ApiResponse<>(true, "", jobInfos);
    }
}
