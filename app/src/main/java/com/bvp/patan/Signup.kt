package com.bvp.patan

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bvp.patan.api.APIInterface
import com.bvp.patan.api.postClient
import com.bvp.patan.operations.Operations
import com.bvp.patan.prefs.SharedPref
import com.bvp.patan.response.GeneralResponse
import kotlinx.android.synthetic.main.signup.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Signup : AppCompatActivity() {

    private lateinit var sharedPref: SharedPref
    private lateinit var operations: Operations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        sharedPref = SharedPref(this)
        operations = Operations(this)

        toolbar()
        handleButtonClicks()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setColorFilter(
            resources.getColor(R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
        toolbar.overflowIcon?.setColorFilter(
            resources.getColor(R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    private fun handleButtonClicks() {
        btnSignup.setOnClickListener {
            // return if empty
            if (operations.checkNullOrEmpty(layoutFirstname)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutMiddlename)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutLastname)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutPassword)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutConfirmNewPassword)) {
                return@setOnClickListener
            }
            if ((!operations.passwordMatch(layoutPassword, layoutConfirmNewPassword))) {
                return@setOnClickListener
            }
            // signup if not empty
            val userMobile = intent.getStringExtra("user_mobile")!!
            val firstName = operations.getValueET(etFirstname)
            val middleName = operations.getValueET(etMiddlename)
            val lastName = operations.getValueET(etLastname)
            val password = operations.getValueET(etPassword)

            userSignup(userMobile, firstName, middleName, lastName, password)
        }
    }

    private fun userSignup(
        userMobile: String,
        firstName: String,
        middleName: String,
        lastName: String,
        password: String
    ) {
        operations.showProgressDialog()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performUserSignup(
            userMobile,
            firstName,
            middleName,
            lastName,
            password
        )

        call.enqueue(object : Callback<GeneralResponse> {
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                operations.hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                if (response.isSuccessful) {
                    when (response.body()!!.response) {
                        "ok" -> {
                            operations.displayToast(getString(R.string.registered_successfully_please_login))
                            finish()
                        }
                        "error" -> {
                            operations.displayToast(getString(R.string.sql_error))
                        }
                        "user_already_registered" -> {
                            //this condition will also never be true(IN ANDROID APP)
                            operations.displayToast(getString(R.string.already_registered))
                        }
                        "user_not_exist" -> {
                            //this condition will also never be true(IN ANDROID APP)
                            operations.displayToast(getString(R.string.user_not_exist))
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