package com.ftg.carrepo.Models

data class PlanDetailsResp (
    val data: PlanData
)
data class PlanData(
    val user_id: String,
    val startDate: String,
    val endDate: String
)