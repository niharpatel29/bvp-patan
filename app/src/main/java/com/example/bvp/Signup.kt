package com.example.bvp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.operations.Operations
import com.example.bvp.prefs.SharedPref
import com.example.bvp.response.GeneralResponse
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

        handleButtonClicks()
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
            if (operations.checkNullOrEmpty(layoutConfirmPassword)) {
                return@setOnClickListener
            }
            if ((!operations.passwordMatch(layoutPassword, layoutConfirmPassword))) {
                return@setOnClickListener
            }
            // signup if not empty
            val userMobile = intent.getStringExtra("user_mobile")
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
                            startActivity(
                                Intent(this@Signup, Login::class.java)
                                    .putExtra("userMobile", userMobile)
                            )
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
