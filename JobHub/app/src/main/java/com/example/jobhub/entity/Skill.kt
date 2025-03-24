package com.example.jobhub.entity

data class Skill(
    val skillId: Int,
    val skillName: String,
    val users: Set<User>? = emptySet(),
    val jobs: Set<Job> = emptySet()
)
