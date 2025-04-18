package com.example.befindingjob.dto;

import com.example.befindingjob.entity.Skill;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
public class SkillDTO {
    private Integer skillId;
    private String skillName;

    @JsonCreator
    public SkillDTO(@JsonProperty("skillId") Integer skillId, @JsonProperty("skillName") String skillName) {
        this.skillId = skillId;
        this.skillName = skillName;
    }

    public SkillDTO(Skill skill) {
        this.skillId = skill.getSkillId();
        this.skillName = skill.getSkillName();
    }
}
