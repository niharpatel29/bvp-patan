package com.example.bvp.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.bvp.R
import com.example.bvp.activities.categories.CalendarEvents
import com.example.bvp.model.ChannelContainer
import com.example.bvp.model.NotificationDataContainer
import com.example.bvp.notification.NotificationHandler
import com.example.bvp.operations.Operations
import com.example.bvp.sqlite.MyDBHandler

class MyReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "MyReceiverTAG"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Operations(context!!).displayToast("Broadcast received")
        Log.d(TAG, "Broadcast received")

        if (MyDBHandler(context).checkBirthdayToday().isNotEmpty()) {
            showNotification(context, context.getString(R.string.type_birthday))
        }
        if (MyDBHandler(context).checkAnniversaryToday().isNotEmpty()) {
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

        val channel = ChannelContainer(
            context,
            context.getString(R.string.channel_birthday_anniversary_id),
            context.getString(R.string.channel_birthday_anniversary_name)
        )
        val notification = NotificationDataContainer(
            title,
            text,
            notificationId,
            R.drawable.ic_baseline_cake_filled,
            R.color.colorRed
        )
        NotificationHandler(context).showNotification(channel, notification, CalendarEvents())
    }
}