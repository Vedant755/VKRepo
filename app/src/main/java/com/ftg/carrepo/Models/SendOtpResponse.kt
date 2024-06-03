package com.ftg.carrepo.Models

data class SendOtpResponse(
    val message: String?,
    val response: SendOtpDetails?,
    val success: Boolean?
)