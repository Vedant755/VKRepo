package com.ftg.carrepo.Utils

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ServerCallInterceptor @Inject constructor() : Interceptor {

    @Inject
    lateinit var tokenManager: SharedPrefManager

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        val token = tokenManager.getToken()
        request.addHeader("Authorization", "Bearer $token")
        return chain.proceed(request.build())
    }
}