package com.example.bvp.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bvp.R
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.operations.Operations
import kotlinx.android.synthetic.main.admin_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminLogin : AppCompatActivity() {

    private lateinit var sharedPrefAdmin: SharedPrefAdmin
    private lateinit var operations: Operations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_login)

        sharedPrefAdmin = SharedPrefAdmin(this)
        operations = Operations(this)

        checkLoginStatus()
        handleButtonClicks()
    }

    private fun checkLoginStatus() {
        if (sharedPrefAdmin.getLoginStatus()) {
            startActivity(Intent(this, AdminRegistersNewUser::class.java))
            finish()
        }
    }

    private fun handleButtonClicks() {
        btnLogin.setOnClickListener {
            // return if empty
            if (operations.checkNullOrEmpty(layoutAdminUsername)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutAdminPassword)) {
                return@setOnClickListener
            }
            // check_user_exist if not empty
            val adminUsername = operations.getValue(layoutAdminUsername)
            val adminPassword = operations.getValue(layoutAdminPassword)

            adminLogin(adminUsername, adminPassword)
        }
    }

    private fun adminLogin(username: String, password: String) {
        operations.showProgressDialog()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performAdminLogin(username, password)

        call.enqueue(object : Callback<AdminLoginModel> {
            override fun onFailure(call: Call<AdminLoginModel>, t: Throwable) {
                operations.hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<AdminLoginModel>,
                response: Response<AdminLoginModel>
            ) {
                if (response.isSuccessful) {
                    when (response.body()!!.response) {
                        "ok" -> {
                            val adminName = response.body()!!.admin.adminName
                            val adminId = response.body()!!.admin.adminId

                            sharedPrefAdmin.run {
                                setLoginStatus(true)
                                setName(adminName)
                                setId(adminId)
                            }

                            startActivity(Intent(this@AdminLogin, AdminActions::class.java))
                            finish()
                        }
                        "failed" -> {
                            operations.displayToast(getString(R.string.login_failed))
                        }
                        "error" -> {
                            operations.displayToast(getString(R.string.sql_error))
                        }
                        else -> {
                            operations.displayToast(getString(R.string.unknown_error))
                        }
                    }
                } else {
                    operations.displayToast(getString(R.string.response_failed))
                }
                operations.hideProgressDialog()
            }
        })
    }
}
