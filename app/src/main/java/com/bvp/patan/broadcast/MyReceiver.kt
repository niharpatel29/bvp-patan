package com.bvp.patan.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bvp.patan.R
import com.bvp.patan.activities.categories.CalendarEvents
import com.bvp.patan.model.ChannelContainer
import com.bvp.patan.model.NotificationDataContainer
import com.bvp.patan.notification.NotificationHandler
import com.bvp.patan.prefs.SharedPref
import com.bvp.patan.sqlite.MyDBHandler

class MyReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "MyReceiverTAG"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "Broadcast received")
        SharedPref(context!!).setBroadcastRegistrationFlag(true)

        val dbHandler = MyDBHandler(context)

        if (SharedPref(context).getLoginStatus()) {
            if (dbHandler.checkBirthdayToday().isNotEmpty()) {
                showNotification(context, context.getString(R.string.type_birthday))
            }
            if (dbHandler.checkAnniversaryToday().isNotEmpty()) {
                showNotification(context, context.getString(R.string.type_anniversary))
            }
        }
    }

    private fun showNotification(context: Context, type: String) {
        // default for birthday
        var notificationId = 89
        var title = context.getString(R.string.birthday)
        val text = context.getString(R.string.tap_here_to_wish)
        var icon = R.drawable.ic_baseline_cake_filled
        val color = R.color.colorRed

        // changes value if anniversary
        if (type == context.getString(R.string.type_anniversary)) {
            notificationId = 99
            title = context.getString(R.string.anniversary)
            icon = R.drawable.ic_baseline_rings
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
            icon,
            color
        )
        NotificationHandler(context).showNotification(channel, notification, CalendarEvents())
    }
}