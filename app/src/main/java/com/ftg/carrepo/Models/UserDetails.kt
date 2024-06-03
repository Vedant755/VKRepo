package com.ftg.carrepo.Models

import com.google.gson.annotations.SerializedName

data class UserDetails(
    @SerializedName("_id")
    val id: String?,
    val name: String?,
    val mobile: String?,
    val address: String?,
    val role: String?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?,
    @SerializedName("__v")
    val v: Int
)