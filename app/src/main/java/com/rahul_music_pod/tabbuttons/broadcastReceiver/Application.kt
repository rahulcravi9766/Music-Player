package com.rahul_music_pod.tabbuttons.broadcastReceiver

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class Application : Application() {
    companion object {
        const val CHANNEL_ID = "channel1"
        const val PLAY = "PLAY"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
    }

    override fun onCreate() {

        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Now Playing Song",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "This is a important channel for showing song!!"
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}