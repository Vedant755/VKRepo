package com.ftg.carrepo.Utils

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {
    val CHANNEL_ID = "VK Enterprises"

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    private fun createChannel(){
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "VK Enterprises",
            NotificationManager.IMPORTANCE_HIGH
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}