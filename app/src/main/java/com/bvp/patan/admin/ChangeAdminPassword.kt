package com.bvp.patan.admin

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bvp.patan.R
import com.bvp.patan.admin.other.AdminGeneralResponse
import com.bvp.patan.admin.other.SharedPrefAdmin
import com.bvp.patan.api.APIInterface
import com.bvp.patan.api.postClient
import com.bvp.patan.operations.*
import kotlinx.android.synthetic.main.change_admin_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangeAdminPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_admin_password)

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
        btnUpdatePassword.setOnClickListener {
            // return if empty
            if (checkNullOrEmpty(layoutOldPassword)) {
                return@setOnClickListener
            }
            if (checkNullOrEmpty(layoutNewPassword)) {
                return@setOnClickListener
            }
            if (checkNullOrEmpty(layoutConfirmNewPassword)) {
                return@setOnClickListener
            }
            if ((!passwordMatch(layoutNewPassword, layoutConfirmNewPassword))) {
                return@setOnClickListener
            }
            // proceed if not empty
            val oldPassword = getValue(layoutOldPassword)
            val newPassword = getValue(layoutNewPassword)

            changePassword(oldPassword, newPassword)
        }
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        showProgressDialog()

        val adminId = SharedPrefAdmin(this).getId()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performChangeAdminPassword(adminId, oldPassword, newPassword)

        call.enqueue(object : Callback<AdminGeneralResponse> {
            override fun onFailure(call: Call<AdminGeneralResponse>, t: Throwable) {
                hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<AdminGeneralResponse>,
                response: Response<AdminGeneralResponse>
            ) {
                if (response.isSuccessful) {
                    val responseMessage = response.body()!!.message
                    when (response.body()!!.response) {
                        "ok" -> {
                            displayToast(responseMessage)
                            finish()
                        }
                        "failed" -> {
                            displayToast(responseMessage)
                        }
                        "error" -> {
                            displayToast(getString(R.string.sql_error))
                        }
                        else -> {
                            displayToast(getString(R.string.unknown_error))
                        }
                    }
                } else {
                    displayToast(getString(R.string.response_failed))
                }
                hideProgressDialog()
            }
        })
    }
}