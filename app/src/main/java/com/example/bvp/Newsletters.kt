package com.example.bvp

import android.Manifest
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bvp.adapter.NewsletterAdapter
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.model.NewsletterListItem
import com.example.bvp.operations.Operations
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
        const val REQUEST_CODE_PERMISSION_SETTING = 101
    }

    private lateinit var operations: Operations
    private lateinit var newsletterAdapter: NewsletterAdapter
    private val newsletterList = ArrayList<NewsletterListItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.newsletters)

        operations = Operations(this)

        toolbar()
        getNewsletters()
        requestStoragePermission()
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

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performGetNewsletters()

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
                                    NewsletterListItem(
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

    private fun requestStoragePermission(): Boolean {
        var granted = false
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
                        Toast.makeText(
                            this@Newsletters,
                            "All permissions are granted!",
                            Toast.LENGTH_SHORT
                        ).show()
                        granted = true
                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        Toast.makeText(
                            this@Newsletters,
                            "Permanently denied",
                            Toast.LENGTH_SHORT
                        ).show()
                        showSettingsDialog()
                        granted = false
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(
                    this,
                    "Error occurred! ",
                    Toast.LENGTH_SHORT
                ).show()
                granted = false
            }
            .onSameThread()
            .check()

        return granted
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