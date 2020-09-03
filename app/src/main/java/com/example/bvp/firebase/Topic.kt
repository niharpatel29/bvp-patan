package com.example.bvp.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

class Topic {

    companion object {
        const val TAG = "TopicTAG"
    }

    val global = "global"
    val login = "login"
    val karobari = "karobari"
    val general = "general"

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