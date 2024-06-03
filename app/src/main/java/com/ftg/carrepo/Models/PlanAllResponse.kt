package com.ftg.carrepo.Models

data class PlanAllResponse (
    val data: List<UserData>
)

data class UserData(
    val _id: String,
    val user_id: String,
    val startDate: String,
    val endDate: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)