package com.example.befindingjob.repository;

import com.example.befindingjob.entity.Application;
import com.example.befindingjob.entity.Job;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.entity.enumm.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {
    List<Application> findByUser(User user);

    List<Application> findByJob(Job job);

    @Query("SELECT a FROM Application a WHERE a.user.userId = :userId AND a.job.jobId = :jobId")
    Optional<Application> findByUserIdAndJobId(@Param("userId") Integer userId, @Param("jobId") Integer jobId);


    @Query("SELECT a FROM Application a WHERE a.job.company.user.userId = :employerId")
    List<Application> findByEmployerId(@Param("employerId") Integer employerId);

    List<Application> findByStatus(ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.company.user.userId = :employerId")
    Long countByEmployerId(@Param("employerId") Integer employerId);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.company.user.userId = :employerId AND a.status = :status")
    Long countByEmployerIdAndStatus(@Param("employerId") Integer employerId, @Param("status") ApplicationStatus status);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Application a WHERE a.user.userId = :userId AND a.job.jobId = :jobId")
    boolean existsByUserIdAndJobId(@Param("userId") Integer userId, @Param("jobId") Integer jobId);
}
