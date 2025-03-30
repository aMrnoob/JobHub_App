package com.example.befindingjob.controller.employer;

import com.example.befindingjob.entity.Skill;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/skill")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @PostMapping("/update")
    public ApiResponse<Void> updateSkill(
            @RequestParam Integer jobId,
            @RequestBody Set<Skill> skills
    ) {
        return skillService.updateSkills(jobId, skills);
    }
}
