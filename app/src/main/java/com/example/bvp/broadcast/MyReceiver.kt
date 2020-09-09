package com.example.bvp.broadcast

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bvp.activities.categories.CalendarEvents
import com.example.bvp.R
import com.example.bvp.operations.Operations

class MyReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "MyReceiverTAG"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Operations(context!!).displayToast("Broadcast received")
        Log.d(TAG, "Broadcast received")

        showNotification(context)
    }

    private fun showNotification(context: Context?) {
        val notificationManager =
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val repeatingIntent = Intent(context, CalendarEvents::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                100,
                repeatingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val builder =
            NotificationCompat.Builder(context, context.getString(R.string.channel_default_id))
                .setContentTitle("Birthday")
                .setContentText("Happy Birthday!!")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_check_circle_filled)
                .setColor(context.resources.getColor(R.color.colorPrimary))

        notificationManager.notify(99, builder.build())
    }
}