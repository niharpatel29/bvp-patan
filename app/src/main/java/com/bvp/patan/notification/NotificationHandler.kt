package com.bvp.patan.notification

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bvp.patan.Login
import com.bvp.patan.R
import com.bvp.patan.model.ChannelContainer
import com.bvp.patan.model.NotificationDataContainer

class NotificationHandler(val context: Context) {

    companion object {
        const val NOTIFICATION_REQUEST_CODE = 101
    }

    private lateinit var notificationManager: NotificationManager

    fun showNotification(
        channel: ChannelContainer?,
        notification: NotificationDataContainer?,
        activity: Activity = Login()
    ) {
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel(channel!!)

        val intent = Intent(context, activity::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val builder =
            NotificationCompat.Builder(context, context.getString(R.string.channel_default_id))
                .setContentTitle(notification!!.title)
                .setContentText(notification.message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(notification.icon)
                .setColor(context.resources.getColor(notification.color))
                .setChannelId(channel.id)

        notificationManager.notify(notification.id, builder.build())
    }

    private fun createChannel(channel: ChannelContainer) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH

            val defaultChannel = NotificationChannel(
                context.getString(R.string.channel_default_id),
                context.getString(R.string.channel_default_name),
                importance
            )

            val notificationChannel = NotificationChannel(
                channel.id,
                channel.name,
                importance
            )

            val channelList = ArrayList<NotificationChannel>()
            channelList.add(defaultChannel)
            channelList.add(notificationChannel)

            notificationManager.createNotificationChannels(channelList)
        }
    }
}