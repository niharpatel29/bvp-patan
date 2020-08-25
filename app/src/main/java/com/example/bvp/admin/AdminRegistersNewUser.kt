package com.example.bvp.admin

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.bvp.R
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.operations.Operations
import kotlinx.android.synthetic.main.admin_registers_new_user.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminRegistersNewUser : AppCompatActivity() {

    private lateinit var sharedPrefAdmin: SharedPrefAdmin
    private lateinit var operations: Operations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_registers_new_user)

        sharedPrefAdmin = SharedPrefAdmin(this)
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
        btnRegisterUser.setOnClickListener {
            // return if empty
            if (operations.checkNullOrEmpty(layoutFirstname)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutLastname)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutPosition)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutUserMobile)) {
                return@setOnClickListener
            }
            // register if not empty
            val adminId = sharedPrefAdmin.getId()
            val firstname = operations.getValue(layoutFirstname)
            val lastname = operations.getValue(layoutLastname)
            val position = operations.getValue(layoutPosition)
            val userMobile = operations.getValue(layoutUserMobile)

            adminRegistersNewUser(adminId, firstname, lastname, position, userMobile)
        }
    }

    private fun adminRegistersNewUser(
        adminId: String?,
        firstName: String,
        lastName: String,
        position: String,
        userMobile: String
    ) {
        operations.showProgressDialog()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performAdminRegistersNewUser(
            adminId,
            firstName,
            lastName,
            position,
            userMobile
        )

        call.enqueue(object : Callback<AdminRegistersNewUserModel> {
            override fun onFailure(call: Call<AdminRegistersNewUserModel>, t: Throwable) {
                operations.hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<AdminRegistersNewUserModel>,
                response: Response<AdminRegistersNewUserModel>
            ) {
                if (response.isSuccessful) {
                    when (response.body()!!.response) {
                        "ok" -> {
                            operations.displayToast(getString(R.string.registration_successful))
                        }
                        "exist" -> {
                            operations.displayToast(getString(R.string.already_registered))
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
