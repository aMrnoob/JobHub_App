package com.example.befindingjob.controller.jobseeker;

import com.example.befindingjob.dto.BookmarkRequest;
import com.example.befindingjob.dto.ItemJobDTO;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.BookmarkService;
import com.example.befindingjob.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobseeker")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private JobService jobService;

    @PostMapping("/book-mark")
    public ApiResponse<Void> bookMark(@RequestBody BookmarkRequest bookmarkRequest) {
        return bookmarkService.bookMark(bookmarkRequest);
    }

    @PostMapping("/delete-book-mark")
    public ApiResponse<Void> deleteBookmark(@RequestBody BookmarkRequest bookmarkRequest) {
        return bookmarkService.deleteBookmark(bookmarkRequest);
    }

    @GetMapping("/get-all-bookmark-jobs")
    public ApiResponse<List<ItemJobDTO>> getBookmarkedJobsByUser(@RequestHeader("token") String token) {
        return jobService.getBookmarkedJobsByUser(token);
    }
}
