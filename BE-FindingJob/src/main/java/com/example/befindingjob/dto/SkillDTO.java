package com.example.befindingjob.dto;

import com.example.befindingjob.entity.Skill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class SkillDTO {
    private Integer skillId;
    private String skillName;

    public SkillDTO(Skill skill) {
        this.skillId = skill.getSkillId();
        this.skillName = skill.getSkillName();
    }
}
