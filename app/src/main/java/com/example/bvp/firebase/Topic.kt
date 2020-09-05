package com.example.bvp.firebase

import android.content.Context
import android.util.Log
import com.example.bvp.R
import com.google.firebase.messaging.FirebaseMessaging

class Topic(context: Context) {

    companion object {
        const val TAG = "TopicTAG"
    }

    val global = context.getString(R.string.topic_global)
    val login = context.getString(R.string.topic_login)
    val karobari = context.getString(R.string.topic_karobari)
    val general = context.getString(R.string.topic_general)

    fun subscribe(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener {
            val message = if (it.isSuccessful) "$topic subscribed" else "$topic subscribe failed"
            Log.d(TAG, message)
        }
    }

    fun unsubscribe(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener {
            val message =
                if (it.isSuccessful) "$topic unsubscribed" else "$topic unsubscribe failed"
            Log.d(TAG, message)
        }
    }
}