package com.example.bvp.admin

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.bvp.R
import com.example.bvp.admin.other.MakeAnnouncementResponse
import com.example.bvp.admin.other.SharedPrefAdmin
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.operations.Operations
import kotlinx.android.synthetic.main.make_announcement.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MakeAnnouncement : AppCompatActivity() {

    private lateinit var sharedPrefAdmin: SharedPrefAdmin
    private lateinit var operations: Operations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.make_announcement)

        sharedPrefAdmin = SharedPrefAdmin(this)
        operations = Operations(this)

        toolbar()
        radioListener()
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
        btnSend.setOnClickListener {
            // return if empty
            if (operations.checkNullOrEmpty(layoutTitle)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutMessage)) {
                return@setOnClickListener
            }
            if (radioGroup.checkedRadioButtonId <= 0) {
                setRadioError(true)
                return@setOnClickListener
            } else {
                setRadioError(false)
            }
            // announce if not empty
            val adminId = sharedPrefAdmin.getId()
            val topic = topic()
            val title = operations.getValue(layoutTitle)
            val message = operations.getValue(layoutMessage)

            makeAnnouncement(adminId, topic, title, message)
        }
    }

    private fun radioListener() {
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            radioKarobari.error = null
            radioGeneral.error = null
            radioAll.error = null
        }
    }

    private fun setRadioError(error: Boolean) {
        if (error) {
            radioKarobari.error = getString(R.string.required_field)
            radioGeneral.error = getString(R.string.required_field)
            radioAll.error = getString(R.string.required_field)
        } else {
            radioKarobari.error = null
            radioGeneral.error = null
            radioAll.error = null
        }
    }

    private fun topic(): String {
        return when {
            radioGeneral.isChecked -> {
                getString(R.string.topic_general)
            }
            radioKarobari.isChecked -> {
                getString(R.string.topic_karobari)
            }
            else -> {
                getString(R.string.topic_login) // send to logged-in users
            }
        }
    }

    private fun makeAnnouncement(
        adminId: String?,
        topic: String,
        title: String,
        message: String
    ) {
        operations.showProgressDialog()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performMakeAnnouncement(
            adminId,
            topic,
            title,
            message
        )

        call.enqueue(object : Callback<MakeAnnouncementResponse> {
            override fun onFailure(call: Call<MakeAnnouncementResponse>, t: Throwable) {
                operations.hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<MakeAnnouncementResponse>,
                response: Response<MakeAnnouncementResponse>
            ) {
                if (response.isSuccessful) {
                    val mResponse = response.body()
                    val responseMessage = mResponse!!.responseMessage
                    when (mResponse.response) {
                        "ok" -> {
                            operations.displayToast(responseMessage)
                        }
                        "error" -> {
                            operations.displayToast(responseMessage)
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