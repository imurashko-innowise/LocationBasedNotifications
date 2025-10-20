package com.sap.codelab.view.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sap.codelab.R
import com.sap.codelab.model.Memo

internal object NotificationsHelper {

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(context: Context, memo: Memo) {
        val notification = NotificationCompat
            .Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location)
            .setContentTitle(memo.title)
            .setContentText(memo.description.take(140))
            .build()

        NotificationManagerCompat.from(context).notify(memo.id.toInt(), notification)
    }

    private const val NOTIFICATION_CHANNEL_ID = "locationNotificationsChannelId"
    private const val NOTIFICATION_CHANNEL_NAME = "Location Notifications"
}