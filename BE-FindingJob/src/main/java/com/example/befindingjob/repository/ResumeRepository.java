package com.example.befindingjob.repository;

import com.example.befindingjob.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Integer> {
    Optional<Resume> findByApplicationId(Integer applicationId);
}
