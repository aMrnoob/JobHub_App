package com.example.befindingjob.service.impl;

import com.example.befindingjob.dto.BookmarkRequest;
import com.example.befindingjob.entity.Job;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.repository.JobRepository;
import com.example.befindingjob.repository.UserRepository;
import com.example.befindingjob.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Override
    public ApiResponse<Void> bookMark(BookmarkRequest bookmarkRequest) {
        Optional<User> userOptional = userRepository.findById(bookmarkRequest.getUserId());
        Optional<Job> jobOptional = jobRepository.findById(bookmarkRequest.getJobId());

        if (userOptional.isEmpty() || jobOptional.isEmpty()) {
            return new ApiResponse<>(false,"Add bookmark failed");
        }

        User user = userOptional.get();
        Job job = jobOptional.get();

        user.getBookmarkedJobs().add(job);
        userRepository.save(user);

        return new ApiResponse<>(true,"Add bookmark successful");
    }

    @Override
    public ApiResponse<Void> deleteBookmark(BookmarkRequest bookmarkRequest) {
        Optional<User> userOptional = userRepository.findById(bookmarkRequest.getUserId());
        Optional<Job> jobOptional = jobRepository.findById(bookmarkRequest.getJobId());

        if (userOptional.isEmpty() || jobOptional.isEmpty()) {
            return new ApiResponse<>(false, "Delete bookmark failed");
        }

        User user = userOptional.get();
        Job job = jobOptional.get();

        if (!user.getBookmarkedJobs().contains(job)) {
            return new ApiResponse<>(false, "Bookmark not found");
        }

        user.getBookmarkedJobs().remove(job);
        userRepository.save(user);

        return new ApiResponse<>(true, "Delete bookmark successful");
    }

}
