package com.bvp.patan.activities.categories

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bvp.patan.R
import com.bvp.patan.adapter.NewsletterAdapter
import com.bvp.patan.api.APIInterface
import com.bvp.patan.api.postClient
import com.bvp.patan.model.ListItemNewsletter
import com.bvp.patan.operations.Operations
import com.bvp.patan.prefs.SharedPref
import com.bvp.patan.response.GetNewsletters
import kotlinx.android.synthetic.main.newsletters.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Newsletters : AppCompatActivity() {

    companion object {
        const val TAG = "NewslettersTAG"
        const val REQUEST_CODE_PERMISSION_SETTING = 101
    }

    private lateinit var operations: Operations
    private lateinit var sharedPref: SharedPref
    private lateinit var newsletterAdapter: NewsletterAdapter
    private val newsletterList = ArrayList<ListItemNewsletter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.newsletters)

        operations = Operations(this)
        sharedPref = SharedPref(this)

        toolbar()
        getNewsletters()
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

    override fun onRestart() {
        super.onRestart()
        newsletterAdapter.notifyDataSetChanged()
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

    private fun getNewsletters() {
        operations.showProgressDialog()

        val userId = sharedPref.getId()
        val userMobile = sharedPref.getMobilePrimary()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performGetNewsletters(userId, userMobile)

        call.enqueue(object : Callback<GetNewsletters> {
            override fun onFailure(call: Call<GetNewsletters>, t: Throwable) {
                operations.hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<GetNewsletters>,
                response: Response<GetNewsletters>
            ) {
                if (response.isSuccessful) {
                    val mResponse = response.body()
                    when (mResponse!!.response) {
                        "ok" -> {
                            recyclerView.layoutManager = LinearLayoutManager(this@Newsletters)
                            newsletterList.clear()

                            val newsletter = mResponse.newsletter
                            for (i in newsletter.indices) {
                                val id = newsletter[i].id
                                val fileName = newsletter[i].fileName
                                val path = newsletter[i].path
                                val type = newsletter[i].type
                                val uploadTime = newsletter[i].uploadTime

                                newsletterList.add(
                                    ListItemNewsletter(
                                        id,
                                        fileName,
                                        path,
                                        type,
                                        uploadTime
                                    )
                                )
                            }

                            newsletterAdapter = NewsletterAdapter(this@Newsletters, newsletterList)
                            recyclerView.adapter = newsletterAdapter
                        }
                        "failed" -> {
                            operations.displayToast(getString(R.string.please_login_again))
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