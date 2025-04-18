package com.example.befindingjob.repository;

import com.example.befindingjob.entity.Company;
import com.example.befindingjob.entity.Job;
import com.example.befindingjob.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Integer> {
    List<Job> findByCompany(Company company);

    List<Job> findByCompanyUserId(Integer userId);
}
