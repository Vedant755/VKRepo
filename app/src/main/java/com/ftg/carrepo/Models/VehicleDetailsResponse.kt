package com.ftg.carrepo.Models

data class VehicleDetailsResponse(
    val data: VehicleDetails?,
    val message: String?,
    val status: Int?,
    val success: Boolean?
)