package com.maxgen.societyguru.firebaseAccess

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.maxgen.societyguru.R
import com.maxgen.societyguru.activity.SplashActivity


class FireMessaging : FirebaseMessagingService() {

    private val CHANNEL_ID = "Bestmarts"
    private val CHANNEL_NAME = "Bestmarts"

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) sendNotification1(message)
        else sendNotification(message)

        if(message.notification!=null){
            Log.d("NOTIFICATION",message.data.toString())
        }
    }

    private fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            taskInfo?.let {
                val componentInfo: ComponentName? = it[0].topActivity
                if (componentInfo?.packageName == context.packageName) {
                    isInBackground = false
                }
            }
        }
        return isInBackground
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        if (!isAppIsInBackground(applicationContext)) {
            val title = remoteMessage.notification!!.title
            val body = remoteMessage.notification!!.body
            val resultIntent = Intent(applicationContext, SplashActivity::class.java)
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setNumber(10)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("Info")
            notificationManager.notify(1, notificationBuilder.build())
        } else {
            val title = remoteMessage.notification?.title
            val body = remoteMessage.notification?.body
            val resultIntent = Intent(applicationContext, SplashActivity::class.java)
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0 /* Request code */,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setNumber(10)
                .setTicker("Bestmarts")
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("Info")
            notificationManager.notify(1, notificationBuilder.build())
        }
    }

    private fun sendNotification1(remoteMessage: RemoteMessage) {
        if (!isAppIsInBackground(applicationContext)) {
            val title = remoteMessage.notification?.title
            val body = remoteMessage.notification?.body
            val resultIntent = Intent(applicationContext, SplashActivity::class.java)
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val oreoNotification = OreoNotification(this)
            val builder: Notification.Builder = oreoNotification.getOreoNotification(
                title,
                body,
                pendingIntent,
                defaultSound,
                (R.mipmap.ic_launcher_round).toString()
            )
            val i = 0
            oreoNotification.manager?.notify(i, builder.build())
        } else {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            val resultIntent = Intent(applicationContext, SplashActivity::class.java)
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val oreoNotification = OreoNotification(this)
            val builder: Notification.Builder = oreoNotification.getOreoNotification(
                title,
                body,
                pendingIntent,
                defaultSound,
                (R.mipmap.ic_launcher_round).toString()
            )
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val i = 0
            oreoNotification.manager?.notify(i, builder.build())
        }
    }

}