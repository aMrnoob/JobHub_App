package com.example.befindingjob.repository;

import com.example.befindingjob.entity.Company;
import com.example.befindingjob.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Integer> {
    List<Job> findByCompany(Company company);
    @Query("SELECT j FROM Job j WHERE j.company.user.userId = :userId")
    List<Job> findByEmployerId(@Param("userId") Integer userId);
    Page<Job> findByCompany(Company company, Pageable pageable);
    @Query("SELECT j FROM Job j WHERE j.expirationDate > CURRENT_TIMESTAMP")
    Page<Job> findAllAvailable(Pageable pageable);
}
