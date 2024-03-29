package com.bvp.patan.prefs

import android.content.Context
import com.bvp.patan.firebase.Topic
import com.bvp.patan.model.UserModel

// USE THIS CLASS FOR AFTER LOGIN PROCESSES FOR BEST RESULTS
class SharedPref(val context: Context) {

    companion object {
        private const val defaultValue = ""
        private const val myPreference = "myPref"

        private const val KEY_LOGIN_STATUS = "login_status"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_MOBILE_PRIMARY = "mobile_primary"
        private const val KEY_FIRSTNAME = "firstname"
        private const val KEY_MIDDLENAME = "middlename"
        private const val KEY_LASTNAME = "lastname"
        private const val KEY_MOBILE_SECONDARY = "mobile_secondary"
        private const val KEY_CATEGORY = "category"
        private const val KEY_POSITION = "position"
        private const val KEY_EMAIL = "email"
        private const val KEY_DOB = "dob"
        private const val KEY_ANNIVERSARY = "anniversary"
        private const val KEY_BLOODGROUP = "bloodgroup"
        private const val KEY_GENDER = "gender"
        private const val KEY_COUNTRY = "country"
        private const val KEY_STATE = "state"
        private const val KEY_CITY = "city"
        private const val KEY_ZIPCODE = "zipcode"
        private const val KEY_RESIDENTIAL_ADDRESS = "residential_address"
        private const val KEY_ADMIN_FLAG = "admin_flag"

        private const val KEY_BROADCAST_REGISTRATION = "broadcast_registration_flag"
    }

    private val sharedPreferences =
        context.getSharedPreferences(myPreference, Context.MODE_PRIVATE)

    fun setBroadcastRegistrationFlag(flag: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_BROADCAST_REGISTRATION, flag)
        editor.apply()
    }

    fun getBroadcastRegistrationFlag(): Boolean {
        return sharedPreferences.getBoolean(KEY_BROADCAST_REGISTRATION, false)
    }

    fun setLoginStatus(loginStatus: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_LOGIN_STATUS, loginStatus)
        editor.apply()
    }

    fun getLoginStatus(): Boolean {
        return sharedPreferences.getBoolean(KEY_LOGIN_STATUS, false)
    }

    fun setFCMToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_FCM_TOKEN, token)
        editor.apply()
    }

    fun getFCMToken(): String? {
        return sharedPreferences.getString(KEY_FCM_TOKEN, defaultValue)
    }

    fun setId(id: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_ID, id)
        editor.apply()
    }

    fun getId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, defaultValue)
    }

    fun setMobilePrimary(mobilePrimary: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_MOBILE_PRIMARY, mobilePrimary)
        editor.apply()
    }

    fun getMobilePrimary(): String? {
        return sharedPreferences.getString(KEY_MOBILE_PRIMARY, defaultValue)
    }

    fun setFirstname(firstname: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_FIRSTNAME, firstname)
        editor.apply()
    }

    fun getFirstname(): String? {
        return sharedPreferences.getString(KEY_FIRSTNAME, defaultValue)
    }

    fun setMiddlename(middlename: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_MIDDLENAME, middlename)
        editor.apply()
    }

    fun getMiddlename(): String? {
        return sharedPreferences.getString(KEY_MIDDLENAME, defaultValue)
    }

    fun setLastname(lastname: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_LASTNAME, lastname)
        editor.apply()
    }

    fun getLastname(): String? {
        return sharedPreferences.getString(KEY_LASTNAME, defaultValue)
    }

    fun setMobileSecondary(mobileSecondary: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_MOBILE_SECONDARY, mobileSecondary)
        editor.apply()
    }

    fun getMobileSecondary(): String? {
        return sharedPreferences.getString(KEY_MOBILE_SECONDARY, defaultValue)
    }

    fun setCategory(category: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_CATEGORY, category)
        editor.apply()
    }

    fun getCategory(): String? {
        return sharedPreferences.getString(KEY_CATEGORY, defaultValue)
    }

    fun setPosition(position: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_POSITION, position)
        editor.apply()
    }

    fun getPosition(): String? {
        return sharedPreferences.getString(KEY_POSITION, defaultValue)
    }

    fun setEmail(email: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_EMAIL, email)
        editor.apply()
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, defaultValue)
    }

    fun setDOB(dob: String?) {
        val editor = sharedPreferences.edit()

        if (dob == "0000-00-00") {
            editor.putString(KEY_DOB, "")
        } else {
            editor.putString(KEY_DOB, dob)
        }
        editor.apply()
    }

    fun getDOB(): String? {
        return sharedPreferences.getString(KEY_DOB, defaultValue)
    }

    fun setAnniversary(anniversary: String?) {
        val editor = sharedPreferences.edit()

        if (anniversary == "0000-00-00") {
            editor.putString(KEY_ANNIVERSARY, "")
        } else {
            editor.putString(KEY_ANNIVERSARY, anniversary)
        }
        editor.apply()
    }

    fun getAnniversary(): String? {
        return sharedPreferences.getString(KEY_ANNIVERSARY, defaultValue)
    }

    fun setBloodGroup(bloodgroup: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_BLOODGROUP, bloodgroup)
        editor.apply()
    }

    fun getBloodGroup(): String? {
        return sharedPreferences.getString(KEY_BLOODGROUP, defaultValue)
    }

    fun setGender(gender: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_GENDER, gender)
        editor.apply()
    }

    fun getGender(): String? {
        return sharedPreferences.getString(KEY_GENDER, defaultValue)
    }

    fun setCountry(country: String?) {
        val editor = sharedPreferences.edit()
//        val defaultCountryValue =
//            context.resources.getStringArray(R.array.spinner_select_category)[0]
//
//        if (country == defaultCountryValue) {
//            editor.putString(KEY_COUNTRY, "")
//        } else {
        editor.putString(KEY_COUNTRY, country)
//        }
        editor.apply()
    }

    fun getCountry(): String? {
        return sharedPreferences.getString(KEY_COUNTRY, defaultValue)
    }

    fun setState(state: String?) {
        val editor = sharedPreferences.edit()
//        val defaultStateValue =
//            context.resources.getStringArray(R.array.spinner_select_category)[0]
//
//        if (state == defaultStateValue) {
//            editor.putString(KEY_STATE, "")
//        } else {
        editor.putString(KEY_STATE, state)
//        }
        editor.apply()
    }

    fun getState(): String? {
        return sharedPreferences.getString(KEY_STATE, defaultValue)
    }

    fun setCity(city: String?) {
        val editor = sharedPreferences.edit()
//        val defaultStateValue =
//            context.resources.getStringArray(R.array.spinner_select_category)[0]
//
//        if (city == defaultStateValue) {
//            editor.putString(KEY_CITY, "")
//        } else {
        editor.putString(KEY_CITY, city)
//        }
        editor.apply()
    }

    fun getCity(): String? {
        return sharedPreferences.getString(KEY_CITY, defaultValue)
    }

    fun setZipcode(zipcode: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_ZIPCODE, zipcode)
        editor.apply()
    }

    fun getZipcode(): String? {
        return sharedPreferences.getString(KEY_ZIPCODE, defaultValue)
    }

    fun setResidentialAddress(residentialAddress: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_RESIDENTIAL_ADDRESS, residentialAddress)
        editor.apply()
    }

    fun getResidentialAddress(): String? {
        return sharedPreferences.getString(KEY_RESIDENTIAL_ADDRESS, defaultValue)
    }

    fun setAdminFlag(flag: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_ADMIN_FLAG, flag)
        editor.apply()
    }

    fun getAdminFlag(): Boolean {
        return sharedPreferences.getBoolean(KEY_ADMIN_FLAG, false)
    }

    fun getAllDetails(): UserModel {
        val userId = getId()
        val mobilePrimary = getMobilePrimary()
        val firstName = getFirstname()
        val middleName = getMiddlename()
        val lastName = getLastname()
        val mobileSecondary = getMobileSecondary()
        val email = getEmail()
        val dob = getDOB()
        val anniversary = getAnniversary()
        val bloodgroup = getBloodGroup()
        val gender = getGender()
        val country = getCountry()
        val state = getState()
        val city = getCity()
        val zipcode = getZipcode()
        val residentialAddress = getResidentialAddress()
        val position = getPosition()
        val category = getCategory()

        return UserModel(
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
    }

    fun clearEditor() {
        val editor = sharedPreferences.edit()
        editor.clear()
        setLoginStatus(false)
        editor.apply()
    }

    fun defaultValue(): String {
        return defaultValue
    }
}
