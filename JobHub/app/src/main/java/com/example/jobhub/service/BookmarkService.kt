package com.example.jobhub.service

import com.example.jobhub.dto.BookmarkRequest
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface BookmarkService {

    @POST("api/jobseeker/book-mark")
    fun bookMark(@Body bookmarkRequest: BookmarkRequest): Call<ApiResponse<Void>>

    @POST("api/jobseeker/delete-book-mark")
    fun deleteBookMark(@Body bookmarkRequest: BookmarkRequest): Call<ApiResponse<Void>>

    @GET("api/jobseeker/get-all-bookmark-jobs")
    fun getBookmarkedJobsByUser(@Header("token") token: String): Call<ApiResponse<List<ItemJobDTO>>>
}