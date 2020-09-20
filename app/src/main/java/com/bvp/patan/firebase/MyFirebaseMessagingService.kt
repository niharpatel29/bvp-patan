package com.bvp.patan.firebase

import android.app.NotificationManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bvp.patan.activities.categories.Announcement
import com.bvp.patan.model.ChannelContainer
import com.bvp.patan.model.NotificationDataContainer
import com.bvp.patan.notification.NotificationHandler
import com.bvp.patan.operations.Operations
import com.bvp.patan.other.SQLiteBackgroundTask
import com.bvp.patan.prefs.SharedPref
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

            // if JSON contains 'channel' object then only show notification
            if (json.has("channel")) {
                showNotification()
            }
            if (json.get("update_sqlite") == true) {
                updateUserListAndData()
            }

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
        Handler(Looper.getMainLooper()).post {
            NotificationHandler(this).showNotification(
                channel(),
                notificationData(),
                Announcement()
            )
        }
    }

    private fun notificationData(): NotificationDataContainer? {
        return try {
            val data = getObjectFromJson("data")

            val title = data.getString("title")
            val message = data.getString("message")

            Log.d(TAG, "title: $title, msg: $message")
            NotificationDataContainer(title, message)
        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            null
        }
    }

    private fun channel(): ChannelContainer? {
        return try {
            val channel = getObjectFromJson("channel")

            val id = channel.getString("id")
            val name = channel.getString("name")

            Log.d(TAG, "id: $id, name: $name")
            ChannelContainer(this, id, name)
        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            null
        }
    }

    private fun updateUserListAndData() {
        try {
            SQLiteBackgroundTask(applicationContext).execute(json)
        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        }
    }
}