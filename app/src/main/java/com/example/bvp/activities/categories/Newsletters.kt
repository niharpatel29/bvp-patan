package com.example.bvp.activities.categories

import android.Manifest
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bvp.R
import com.example.bvp.adapter.NewsletterAdapter
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.model.ListItemNewsletter
import com.example.bvp.operations.Operations
import com.example.bvp.prefs.SharedPref
import com.example.bvp.response.GetNewsletters
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
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
        requestStoragePermission()
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

    private fun requestStoragePermission() {
        Dexter
            .withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        Log.d(TAG, "Permission granted")
                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        operations.displayToast("Permanently denied")
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
                operations.displayToast("Error occurred")
            }
            .onSameThread()
            .check()
    }

    private fun showSettingsDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Need Permissions")
        alertDialog.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        alertDialog.setPositiveButton("GOTO SETTINGS") { dialog, id ->
            dialog.cancel()
            openSettings()
        }
        alertDialog.setNegativeButton("Cancel") { dialog, id ->
            dialog.cancel()
        }
        alertDialog.show()
    }

    private fun openSettings() {
        // navigating user to app settings
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, REQUEST_CODE_PERMISSION_SETTING)
    }
}