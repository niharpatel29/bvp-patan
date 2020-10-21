package com.bvp.patan.background_worker

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.bvp.patan.model.UserModel
import com.bvp.patan.operations.logout
import com.bvp.patan.prefs.SharedPref
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
            val sharedPref = SharedPref(context)

            val user = json[0]!!.getJSONObject("user")
            val userId = user.getString("user_id")

            if (json[0]!!.has("operation")) {
                if (json[0]!!.getString("operation") == "delete_user") {

                    val dbHandler = MyDBHandler(context)
                    if (dbHandler.isUserExist(userId)) {
                        dbHandler.deleteUser(userId)

                        if (userId == sharedPref.getId()) {
                            context.logout()
                        }
                    }
                    return "User deleted successfully"
                }
            }

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
            val category = user.getString("category")

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
                position,
                category
            )

            // update personal info
            if (sharedPref.getLoginStatus()) {
                if (userDetails.userId == sharedPref.getId()) {
                    try {
                        sharedPref.run {
                            setMobilePrimary(mobilePrimary)
                            setFirstname(firstName)
                            setMiddlename(middleName)
                            setLastname(lastName)
                            setMobileSecondary(mobileSecondary)
                            setEmail(email)
                            setDOB(dob)
                            setAnniversary(anniversary)
                            setBloodGroup(bloodgroup)
                            setGender(gender)
                            setCountry(country)
                            setState(state)
                            setCity(city)
                            setZipcode(zipcode)
                            setResidentialAddress(residentialAddress)
                            setPosition(position)
                            setCategory(category)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, e.message.toString())
                    }
                }
            }
            if (MyDBHandler(context).isUserExist(userDetails.userId)) {
                MyDBHandler(context).updateUserDetails(userDetails)
                Log.d(TAG, "exist: ${userDetails.userId}")
            } else {
                MyDBHandler(context).addUser(userDetails)
                Log.d(TAG, "notExist: ${userDetails.userId}")
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        }
        return "Data updated successfully"
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        Log.d(TAG, "result: $result")
    }
}