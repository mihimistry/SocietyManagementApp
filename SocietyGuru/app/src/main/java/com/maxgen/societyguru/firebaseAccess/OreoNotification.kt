package com.maxgen.societyguru.firebaseAccess

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat


class OreoNotification(base: Context?) : ContextWrapper(base) {
    private var notificationManager: NotificationManager? = null

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Fcm Test channel for app test FCM"
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        channel.setShowBadge(false)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        manager!!.createNotificationChannel(channel)
    }

    val manager: NotificationManager?
        get() {
            if (notificationManager == null)
                notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager
        }

    @TargetApi(Build.VERSION_CODES.O)
    fun getOreoNotification(
        title: String?,
        body: String?,
        pendingIntent: PendingIntent?,
        soundUri: Uri?,
        icon: String?
    ): Notification.Builder = Notification.Builder(applicationContext, CHANNEL_ID)
        .setAutoCancel(true)
        .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
        .setDefaults(Notification.DEFAULT_ALL)
        .setWhen(System.currentTimeMillis())
        .setSmallIcon(com.maxgen.societyguru.R.mipmap.ic_launcher_round)
        .setTicker("Fcm Test")
        .setNumber(10)
        .setContentTitle(title)
        .setContentText(body)
        .setContentIntent(pendingIntent)
        .setContentInfo("Info")


    companion object {
        private const val CHANNEL_ID = "Fcm Test"
        private const val CHANNEL_NAME = "Fcm Test"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }
}