package com.example.bvp.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bvp.R
import com.example.bvp.activities.categories.Announcement
import com.example.bvp.operations.Operations
import com.example.bvp.other.SQLServices
import com.example.bvp.prefs.SharedPref
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val TAG = "fcmTAG"
    }

    private lateinit var json: JSONObject
    private lateinit var notificationManager: NotificationManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "token: $token")

        SharedPref(applicationContext).setFCMToken(token) //working
        Handler(Looper.getMainLooper()).post {
            Operations(applicationContext).displayToast("token updated")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            json = JSONObject(remoteMessage.data.toString())
            showNotification()
//            getUpdatedUserDetails()

            Log.d(TAG, "From: ${remoteMessage.from}")
            Log.d(TAG, "JSON data: $json")
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    private fun getObjectFromJson(obj: String): JSONObject {
        Log.d(TAG, json.getJSONObject(obj).toString())
        return json.getJSONObject(obj)
    }

    private fun showNotification() {
        if (getData() == null) {
            Log.e(TAG, "data object null")
            return
        }

        createChannel()
        val title = getData()?.title
        val message = getData()?.message

        val intent = Intent(this, Announcement::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder =
            NotificationCompat.Builder(this, getString(R.string.channel_default_id))
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_check_circle_filled)
                .setColor(resources.getColor(R.color.colorPrimary))
                .setChannelId(getChannel()!!.id)

        notificationManager.notify(999, builder.build())
    }

    data class Data(val title: String, val message: String)

    private fun getData(): Data? {
        return try {
            val data = getObjectFromJson("data")

            val title = data.getString("title")
            val message = data.getString("message")

            Log.d(TAG, "title: $title, msg: $message")
            Data(title, message)
        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            null
        }
    }

    private fun createChannel() {
        val channelId = getChannel()?.id
        val channelName = getChannel()?.name

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH

            val defaultChannel = NotificationChannel(
                getString(R.string.channel_default_id),
                getString(R.string.channel_default_name),
                importance
            )

            val channel = NotificationChannel(
                channelId,
                channelName,
                importance
            )

            val channelList = ArrayList<NotificationChannel>()
            channelList.add(defaultChannel)
            channelList.add(channel)

            notificationManager.createNotificationChannels(channelList)
        }
    }

    data class Channel(val id: String, val name: String)

    private fun getChannel(): Channel? {
        return try {
            val channel = getObjectFromJson("channel")

            val id = channel.getString("id")
            val name = channel.getString("name")

            Log.d(TAG, "id: $id, name: $name")
            Channel(id, name)
        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            null
        }
    }

    private fun getUpdatedUserDetails() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(
                    Intent(applicationContext, SQLServices::class.java)
                        .putExtra("user", json.toString())
                )
            } else {
                startService(
                    Intent(applicationContext, SQLServices::class.java)
                        .putExtra("user", json.toString())
                )
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        }
    }
}