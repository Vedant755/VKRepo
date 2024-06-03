package com.ftg.carrepo.Models

data class LoadBranchResponse(
    val success: Boolean,
    val data: List<Finance>

)
data class Finance(
    val id: Int,
    val table_no: Int,
    val financeName: String,
    val headofficename: String,
    val createDate: String
)