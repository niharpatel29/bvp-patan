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
import com.bvp.patan.operations.Operations
import kotlinx.android.synthetic.main.upload_photos_link.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UploadPhotosLink : AppCompatActivity() {

    companion object {
        const val TAG = "UploadPhotosLinkTAG"
    }

    lateinit var operations: Operations
    lateinit var sharedPrefAdmin: SharedPrefAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_photos_link)

        operations = Operations(this)
        sharedPrefAdmin = SharedPrefAdmin(this)

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
        btnUpload.setOnClickListener {
            // return if empty
            if (operations.checkNullOrEmpty(layoutLink)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutDesc)) {
                return@setOnClickListener
            }
            // upload if not empty
            val adminId = sharedPrefAdmin.getId()
            val link = operations.getValue(layoutLink)
            val description = operations.getValue(layoutDesc)

            uploadPhotoLink(adminId, link, description)
        }
    }

    private fun uploadPhotoLink(adminId: String?, link: String, description: String) {
        operations.showProgressDialog()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performUploadPhotosLink(adminId, link, description)

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
                    val mResponse = response.body()
                    val responseMessage = mResponse!!.message
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