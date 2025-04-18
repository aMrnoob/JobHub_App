package com.example.befindingjob.repository;

import com.example.befindingjob.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Integer> {
    @Query("SELECT r FROM Resume r WHERE r.application.applicationId = :applicationId")
    Optional<Resume> findByApplicationId(@Param("applicationId") Integer applicationId);
}
