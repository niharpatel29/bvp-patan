package com.bvp.patan.background_worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bvp.patan.broadcast.MyReceiver
import com.bvp.patan.prefs.SharedPref
import java.util.*

class RegisterAlarmBroadcast(val context: Context) : AsyncTask<Void, Void, Boolean>() {

    companion object {
        const val TAG = "RegisterAlarmBroadcastTAG"
    }

    override fun doInBackground(vararg p0: Void?): Boolean {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        Log.d(TAG, calendar.get(Calendar.YEAR).toString())
        Log.d(TAG, calendar.get(Calendar.MONTH).toString())
        Log.d(TAG, calendar.get(Calendar.DAY_OF_MONTH).toString())
        Log.d(TAG, calendar.get(Calendar.HOUR_OF_DAY).toString())
        Log.d(TAG, calendar.get(Calendar.MINUTE).toString())
        Log.d(TAG, calendar.get(Calendar.SECOND).toString())

        //creating a new intent specifying the broadcast receiver
        val intent = Intent(context.applicationContext, MyReceiver::class.java)
        //creating a pending intent using the intent
        val pendingIntent =
            PendingIntent.getBroadcast(
                context.applicationContext,
                100,
                intent,
                PendingIntent.FLAG_NO_CREATE
            )

        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

        //setting the repeating alarm that will be triggered every day
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        return true
    }

    override fun onPostExecute(registrationStatus: Boolean) {
        super.onPostExecute(registrationStatus)
        if (registrationStatus) {
            SharedPref(context).setBroadcastRegistrationFlag(true)
            Log.d(TAG, "registered")
        }
    }
}