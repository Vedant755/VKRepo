package com.ftg.carrepo.Models

data class DeleteResponse(
    val data: List<RemainingVehicleDetails?>?,
    val message: String?,
    val status: Int?,
    val success: Boolean?
)