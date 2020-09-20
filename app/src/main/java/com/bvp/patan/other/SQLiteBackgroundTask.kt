package com.bvp.patan.other

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.bvp.patan.firebase.MyFirebaseMessagingService
import com.bvp.patan.model.UserModel
import com.bvp.patan.sqlite.MyDBHandler
import org.json.JSONException
import org.json.JSONObject

class SQLiteBackgroundTask(val context: Context) : AsyncTask<JSONObject, Void, String>() {
    companion object {
        const val TAG = "SQLiteBackgroundTaskTAG"
    }

    override fun onPreExecute() {
        super.onPreExecute()
        Log.d(TAG, "operation started")
    }

    override fun doInBackground(vararg json: JSONObject?): String? {
        try {
            val user = json[0]!!.getJSONObject("user")

            val userId = user.getString("user_id")
            val mobilePrimary = user.getString("mobile_primary")
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
            val position = user.getString("position")

            val userDetails = UserModel(
                userId,
                mobilePrimary,
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
                position
            )

            if (MyDBHandler(context).isUserExist(userDetails.userId)) {
                Log.d(TAG, "exist: ${userDetails.userId}")
                MyDBHandler(context).updateUserDetails(userDetails)
            } else {
                Log.d(TAG, "notExist: ${userDetails.userId}")
                MyDBHandler(context).addUser(userDetails)
            }
        } catch (e: JSONException) {
            Log.e(MyFirebaseMessagingService.TAG, "Json Exception: ${e.message}")
        } catch (e: Exception) {
            Log.e(MyFirebaseMessagingService.TAG, "Exception: ${e.message}")
        }
        return "Data updated successfully"
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        Log.d(TAG, "result: $result")
    }
}