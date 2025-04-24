package com.example.befindingjob.service;

import com.example.befindingjob.dto.BookmarkRequest;
import com.example.befindingjob.model.ApiResponse;

public interface BookmarkService {
    ApiResponse<Void> bookMark(BookmarkRequest bookmarkRequest);
    ApiResponse<Void> deleteBookmark(BookmarkRequest bookmarkRequest);
}
