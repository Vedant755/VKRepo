package com.ftg.carrepo.Models

data class VehicleAdminDetailsResponse(
    val data: VehicleAdminDetailsResponseData
)
data class VehicleAdminDetailsResponseData(
    val branch_id: List<String>,
    val data_count: Int,
    val branch_count: Int,
    val duplicates: List<String>,
    val convertedBranchIds: List<String>,
    val branches: List<Branch>,
    val vehicles: List<VehicleDetails>
)


