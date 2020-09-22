package com.bvp.patan.activities.categories

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.RatingBar.OnRatingBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.bvp.patan.R
import com.bvp.patan.api.APIInterface
import com.bvp.patan.api.postClient
import com.bvp.patan.operations.Operations
import com.bvp.patan.prefs.SharedPref
import com.bvp.patan.response.GeneralResponse
import kotlinx.android.synthetic.main.feedback.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Feedback : AppCompatActivity() {

    companion object {
        const val TAG = "FeedbackTAG"
    }

    lateinit var operations: Operations
    lateinit var sharePref: SharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feedback)

        operations = Operations(this)
        sharePref = SharedPref(this)

        toolbar()
        setMinimumRating()
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
        btnSubmit.setOnClickListener {
            if (ratingBar.rating < 1.0f) {
                operations.displayToast(getString(R.string.rating_is_required))
                return@setOnClickListener
            }
            if (operations.checkNullOrEmptyET(etFeedback)) {
                return@setOnClickListener
            }
            // send if not empty
            val rating = ratingBar.rating.toString()
            val feedback = operations.getValueET(etFeedback)
            sendFeedback(rating, feedback)
        }
    }

    private fun setMinimumRating() {
        ratingBar.onRatingBarChangeListener =
            OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                if (rating < 1.0f) ratingBar.rating = 1.0f
            }
    }

    private fun sendFeedback(rating: String, feedback: String) {
        operations.showProgressDialog()

        val userId = sharePref.getId()
        val brand = Build.BRAND
        val model = Build.MODEL
        val device = Build.DEVICE
        val version = Build.VERSION.RELEASE

        Log.d(TAG, brand)
        Log.d(TAG, model)
        Log.d(TAG, device)
        Log.d(TAG, version)

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call =
            apiService.performSendFeedback(userId, rating, feedback, brand, model, device, version)

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