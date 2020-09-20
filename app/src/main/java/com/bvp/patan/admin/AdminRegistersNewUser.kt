package com.bvp.patan.admin

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bvp.patan.R
import com.bvp.patan.admin.other.AdminGeneralResponse
import com.bvp.patan.admin.other.SharedPrefAdmin
import com.bvp.patan.api.APIInterface
import com.bvp.patan.api.postClient
import com.bvp.patan.operations.Operations
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
        setVisibility()
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

    private fun setVisibility() {
        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (radioGeneral.isChecked) {
                layoutPosition.visibility = View.GONE
                layoutPosition.editText!!.text = null
            } else {
                layoutPosition.visibility = View.VISIBLE
            }
        }
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
            if (operations.checkNullOrEmpty(layoutUserMobile)) {
                return@setOnClickListener
            }
            if (radioGroup.checkedRadioButtonId <= 0) {
                setRadioError(true)
                return@setOnClickListener
            } else {
                setRadioError(false)
            }
            // register if not empty
            val adminId = sharedPrefAdmin.getId()
            val firstname = operations.getValue(layoutFirstname)
            val lastname = operations.getValue(layoutLastname)
            val userMobile = operations.getValue(layoutUserMobile)
            val category =
                if (radioGeneral.isChecked) getString(R.string.topic_general) else getString(R.string.topic_karobari)
            val position = operations.getValue(layoutPosition)

            adminRegistersNewUser(adminId, firstname, lastname, category, position, userMobile)
        }
    }

    private fun setRadioError(error: Boolean) {
        if (error) {
            radioKarobari.error = getString(R.string.required_field)
            radioGeneral.error = getString(R.string.required_field)
        } else {
            radioKarobari.error = null
            radioGeneral.error = null
        }
    }

    private fun adminRegistersNewUser(
        adminId: String?,
        firstName: String,
        lastName: String,
        category: String,
        position: String,
        userMobile: String
    ) {
        operations.showProgressDialog()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performAdminRegistersNewUser(
            adminId,
            firstName,
            lastName,
            category,
            position,
            userMobile
        )

        call.enqueue(object : Callback<AdminGeneralResponse> {
            override fun onFailure(call: Call<AdminGeneralResponse>, t: Throwable) {
                operations.hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<AdminGeneralResponse>,
                response: Response<AdminGeneralResponse>
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