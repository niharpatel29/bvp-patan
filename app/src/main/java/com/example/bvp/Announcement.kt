package com.example.bvp

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.operations.Operations
import com.example.bvp.response.GetAnnouncement
import kotlinx.android.synthetic.main.announcement.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Announcement : AppCompatActivity() {

    companion object {
        const val TAG = "AnnouncementTAG"
    }

    private lateinit var operations: Operations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.announcement)

        operations = Operations(this)

        toolbar()
        handleButtonClicks()
        getAnnouncement()
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
        /*btnMarkRead.setOnClickListener {
            finish()
        }*/
    }

    private fun getAnnouncement() {
        operations.showProgressDialog()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performGetAnnouncement()

        call.enqueue(object : Callback<GetAnnouncement> {
            override fun onFailure(call: Call<GetAnnouncement>, t: Throwable) {
                operations.hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<GetAnnouncement>,
                response: Response<GetAnnouncement>
            ) {
                if (response.isSuccessful) {
                    val mResponse = response.body()
                    when (mResponse!!.response) {
                        "ok" -> {
                            val title = mResponse.title
                            val message = mResponse.message

                            tvTitle.text = title
                            tvMessage.text = message
                            showAnnouncement(true)
                        }
                        "null" -> {
                            showAnnouncement(false)
                        }
                        else -> {
                            operations.displayToast(getString(R.string.unknown_error))
                            showAnnouncement(false)
                        }
                    }
                } else {
                    operations.displayToast(getString(R.string.response_failed))
                    showAnnouncement(false)
                }
                operations.hideProgressDialog()
            }
        })
    }

    private fun showAnnouncement(flag: Boolean) {
        if (flag) {
            tvTemp.visibility = View.GONE
            cardView.visibility = View.VISIBLE
        } else {
            tvTemp.visibility = View.VISIBLE
            cardView.visibility = View.GONE
        }
    }
}