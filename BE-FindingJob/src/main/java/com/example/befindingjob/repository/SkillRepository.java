package com.example.befindingjob.repository;

import com.example.befindingjob.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {
    Skill findBySkillName(String skillName);
}
