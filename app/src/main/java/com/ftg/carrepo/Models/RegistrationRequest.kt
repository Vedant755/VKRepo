package com.ftg.carrepo.Models

data class RegistrationRequest(
    val name: String,
    val mobile: String,
    val address: String,
    val role: String,
    val otp: String
)