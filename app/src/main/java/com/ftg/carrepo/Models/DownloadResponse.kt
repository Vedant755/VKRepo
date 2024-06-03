package com.ftg.carrepo.Models

data class DownloadResponse(
    val message: String?,
    val status: Int?,
    val data: List<VehicleDetails?>?,
    val success: Boolean?,
    val total: Int?
)