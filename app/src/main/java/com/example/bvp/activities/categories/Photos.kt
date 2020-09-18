package com.example.bvp.activities.categories

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bvp.R
import com.example.bvp.adapter.PhotosAdapter
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.model.ListItemPhotos
import com.example.bvp.operations.Operations
import com.example.bvp.prefs.SharedPref
import com.example.bvp.response.GetPhotosLinks
import kotlinx.android.synthetic.main.photos.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Photos : AppCompatActivity() {

    companion object {
        const val TAG = "PhotosTAG"
    }

    lateinit var operations: Operations
    lateinit var sharedPref: SharedPref
    lateinit var photosAdapter: PhotosAdapter
    private val photosList = ArrayList<ListItemPhotos>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.photos)

        operations = Operations(this)
        sharedPref = SharedPref(this)

        toolbar()
        getPhotosLinks()
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

    private fun getPhotosLinks() {
        operations.showProgressDialog()

        val userId = sharedPref.getId()
        val userMobile = sharedPref.getMobilePrimary()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performGetPhotosLinks(userId, userMobile)

        call.enqueue(object : Callback<GetPhotosLinks> {
            override fun onFailure(call: Call<GetPhotosLinks>, t: Throwable) {
                operations.hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<GetPhotosLinks>,
                response: Response<GetPhotosLinks>
            ) {
                if (response.isSuccessful) {
                    val mResponse = response.body()
                    when (mResponse!!.response) {
                        "ok" -> {
                            recyclerView.layoutManager = LinearLayoutManager(this@Photos)
                            photosList.clear()

                            val photo = mResponse.photos
                            for (i in photo.indices) {
                                val id = photo[i].id
                                val link = photo[i].link
                                val desc = photo[i].description
                                val time = photo[i].time

                                photosList.add(ListItemPhotos(id, link, desc, time))
                            }

                            photosAdapter = PhotosAdapter(this@Photos, photosList)
                            recyclerView.adapter = photosAdapter
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