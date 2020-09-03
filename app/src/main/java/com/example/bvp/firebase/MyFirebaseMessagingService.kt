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
import com.example.bvp.Login
import com.example.bvp.R
import com.example.bvp.operations.Operations
import com.example.bvp.prefs.SharedPref
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val TAG = "fcmTAG"
    }

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
        try {
            Log.d(TAG, "From: ${remoteMessage.from}")
            Log.d(TAG, "To: ${remoteMessage.to}")

            val json = JSONObject(remoteMessage.data.toString())
            showNotification(json)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    data class JsonData(val title: String, val message: String)

    private fun showNotification(json: JSONObject) {
        if (getDataFromJson(json) == null) {
            Log.e(TAG, "json data is null")
            return
        }

        val title = getDataFromJson(json)?.title
        val message = getDataFromJson(json)?.message

        val intent = Intent(this, Login::class.java)
        intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder =
            NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_check_circle_filled)
                .setColor(resources.getColor(R.color.colorPrimary))

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationChannel(notificationManager)
        notificationManager.notify(999, builder.build())
    }

    private fun getDataFromJson(json: JSONObject): JsonData? {
        Log.d(TAG, "JSON data: $json")
        return try {
            val data = json.getJSONObject("data")

            val title = data.getString("title")
            val message = data.getString("message")

            Log.d(TAG, "title: $title, msg: $message")
            JsonData(title, message)
        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            null
        }
    }

    private fun notificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                getString(R.string.default_notification_channel_id),
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}