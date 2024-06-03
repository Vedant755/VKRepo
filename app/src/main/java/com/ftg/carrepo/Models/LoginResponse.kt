package com.ftg.carrepo.Models

data class LoginResponse(
    val data: UserDetails?,
    val token: String?
)