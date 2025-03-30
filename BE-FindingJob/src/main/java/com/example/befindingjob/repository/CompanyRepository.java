package com.example.befindingjob.repository;

import com.example.befindingjob.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
    Optional<Company> findByCompanyName(String companyName);
    @Query("SELECT c FROM Company c WHERE c.user.userId = :userId")
    List<Company> findByUserId(@Param("userId") Integer userId);
}
