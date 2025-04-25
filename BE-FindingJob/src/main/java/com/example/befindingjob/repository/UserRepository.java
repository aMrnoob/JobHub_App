package com.example.befindingjob.repository;

import com.example.befindingjob.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END " +
            "FROM User u JOIN u.bookmarkedJobs j " +
            "WHERE u.userId = :userId AND j.jobId = :jobId")
    boolean isBookmarked(@Param("userId") Integer userId, @Param("jobId") Integer jobId);
}
