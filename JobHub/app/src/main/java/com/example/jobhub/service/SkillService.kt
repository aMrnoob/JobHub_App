package com.example.jobhub.service

import com.example.jobhub.entity.Skill
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface SkillService {
    @POST("api/skill/update")
    fun updateSkills(
        @Query("jobId") jobId: Int,
        @Body skills: Set<Skill>
    ): Call<ApiResponse<Void>>
}