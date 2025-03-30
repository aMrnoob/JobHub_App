package com.example.befindingjob.repository;

import com.example.befindingjob.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {
    List<Skill> findBySkillIdIn(Set<Integer> skillIds);
    Optional<Skill> findBySkillName(String skillName);
}
