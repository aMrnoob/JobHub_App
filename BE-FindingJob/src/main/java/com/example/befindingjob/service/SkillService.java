package com.example.befindingjob.service;

import com.example.befindingjob.entity.Skill;
import com.example.befindingjob.model.ApiResponse;

import java.util.Set;

public interface SkillService {
    ApiResponse<Void> updateSkills(Integer jobId, Set<Skill> skills);
}
