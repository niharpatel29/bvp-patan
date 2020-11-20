package com.bvp.patan.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bvp.patan.background_worker.RegisterAlarmBroadcast
import com.bvp.patan.prefs.SharedPref

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // alarm repeating broadcast automatically disabled. So, have to make changes in Pref file.
        SharedPref(context).setBroadcastRegistrationFlag(false)

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            RegisterAlarmBroadcast(context).execute()
        }
    }
}