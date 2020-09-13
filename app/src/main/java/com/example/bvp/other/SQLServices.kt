package com.example.bvp.other

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.example.bvp.firebase.MyFirebaseMessagingService
import com.example.bvp.model.UserModel
import com.example.bvp.sqlite.MyDBHandler
import org.json.JSONException
import org.json.JSONObject

class SQLServices(name: String = "test-service") : IntentService(name) {

    companion object {
        const val TAG = "IntentServicesTAG"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent")

        try {
            val json = JSONObject(intent!!.getStringExtra("user")!!)
            val user = json.getJSONObject("user")

            val userId = user.getString("user_id")
            val firstName = user.getString("first_name")
            val middleName = user.getString("middle_name")
            val lastName = user.getString("last_name")
            val mobileSecondary = user.getString("mobile_secondary")
            val email = user.getString("email")
            val dob = user.getString("dob")
            val anniversary = user.getString("anniversary")
            val bloodgroup = user.getString("bloodgroup")
            val gender = user.getString("gender")
            val country = user.getString("country")
            val state = user.getString("state")
            val city = user.getString("city")
            val zipcode = user.getString("zipcode")
            val residentialAddress = user.getString("residential_address")

            val userDetails = UserModel(
                userId,
                null,
                firstName,
                middleName,
                lastName,
                mobileSecondary,
                email,
                dob,
                anniversary,
                bloodgroup,
                gender,
                country,
                state,
                city,
                zipcode,
                residentialAddress,
                null
            )

            MyDBHandler(applicationContext).updateUserDetails(userDetails)
        } catch (e: JSONException) {
            Log.e(MyFirebaseMessagingService.TAG, "Json Exception: ${e.message}")
        } catch (e: Exception) {
            Log.e(MyFirebaseMessagingService.TAG, "Exception: ${e.message}")
        }
    }
}