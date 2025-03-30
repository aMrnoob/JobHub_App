package com.example.befindingjob.service.impl;

import com.example.befindingjob.entity.Job;
import com.example.befindingjob.entity.Skill;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.repository.JobRepository;
import com.example.befindingjob.repository.SkillRepository;
import com.example.befindingjob.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class SkillServiceImpl implements SkillService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Override
    public ApiResponse<Void> updateSkills(Integer jobId, Set<Skill> skills) {
        Optional<Job> existingJobOpt = jobRepository.findById(jobId);
        if (existingJobOpt.isEmpty()) {
            return new ApiResponse<>(false, "Job not found.");
        }

        Job job = existingJobOpt.get();

        if (skills != null && !skills.isEmpty()) {
            Set<Skill> existingSkills = new HashSet<>();
            for (Skill skill : skills) {
                Optional<Skill> existingSkillOpt = skillRepository.findBySkillName(skill.getSkillName());

                if (existingSkillOpt.isPresent()) {
                    existingSkills.add(existingSkillOpt.get());
                } else {
                    Skill newSkill = skillRepository.save(skill);
                    existingSkills.add(newSkill);
                }
            }
            job.setRequiredSkills(existingSkills);
        }

        jobRepository.save(job);
        return new ApiResponse<>(true, "Job skills updated successfully.");
    }
}
