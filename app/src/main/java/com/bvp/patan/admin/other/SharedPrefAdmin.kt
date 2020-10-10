package com.bvp.patan.admin.other

import android.content.Context

class SharedPrefAdmin(private val context: Context) {

    private val adminPreference = "adminPref"
    private var KEY_ADMIN_LOGIN_STATUS = "admin_login_status"
    private var KEY_ADMIN_NAME = "admin_name"
    private var KEY_ADMIN_ID = "admin_id"

    private var sharedPreferences =
        context.getSharedPreferences(adminPreference, Context.MODE_PRIVATE)

    fun setLoginStatus(status: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_ADMIN_LOGIN_STATUS, status)
        editor.apply()
    }

    fun getLoginStatus(): Boolean {
        return sharedPreferences.getBoolean(KEY_ADMIN_LOGIN_STATUS, false)
    }

    fun setId(id: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_ADMIN_ID, id)
        editor.apply()
    }

    fun getId(): String? {
        return sharedPreferences.getString(KEY_ADMIN_ID, "id")
    }

    fun adminLogout() {
        val editor = sharedPreferences.edit()
        editor.clear()
        setLoginStatus(false)
        editor.apply()
    }
}