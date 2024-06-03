package com.ftg.carrepo.Models

data class SearchAdminVehicleData (
    val searchTerm: String?,
    val type: String?,
    val pageIndex: Int?,
    val pageSize: Int?
)
