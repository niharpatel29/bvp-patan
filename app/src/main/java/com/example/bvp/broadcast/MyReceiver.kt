package com.example.bvp.broadcast

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bvp.R
import com.example.bvp.activities.categories.CalendarEvents
import com.example.bvp.operations.Operations
import com.example.bvp.sqlite.MyDBHandler

class MyReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "MyReceiverTAG"
        const val NOTIFICATION_REQUEST_CODE = 101
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Operations(context!!).displayToast("Broadcast received")
        Log.d(TAG, "Broadcast received")

        if (MyDBHandler(context).anyBirthdayToday()) {
            showNotification(context, context.getString(R.string.type_birthday))
        }
        if (MyDBHandler(context).anyAnniversaryToday()) {
            showNotification(context, context.getString(R.string.type_anniversary))
        }
    }

    private fun showNotification(context: Context?, type: String) {
        // default for birthday
        var notificationId = 89
        var title = context!!.getString(R.string.birthday)
        val text = context.getString(R.string.tap_here_to_wish)

        // changes value if anniversary
        if (type == context.getString(R.string.type_anniversary)) {
            notificationId = 99
            title = context.getString(R.string.anniversary)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val repeatingIntent = Intent(context, CalendarEvents::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                NOTIFICATION_REQUEST_CODE,
                repeatingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val builder =
            NotificationCompat.Builder(context, context.getString(R.string.channel_default_id))
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_cake_filled)
                .setColor(context.resources.getColor(R.color.colorRed))
                .setChannelId(context.getString(R.string.channel_default_id))

        notificationManager.notify(notificationId, builder.build())
    }
}