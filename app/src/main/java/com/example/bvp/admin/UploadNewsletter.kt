package com.example.bvp.admin

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.bvp.R
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.operations.Operations
import com.example.bvp.response.UploadResponse
import com.example.bvp.upload.UploadRequestBody
import kotlinx.android.synthetic.main.newsletters.*
import kotlinx.android.synthetic.main.upload_newsletter.*
import kotlinx.android.synthetic.main.upload_newsletter.toolbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class UploadNewsletter : AppCompatActivity() {

    companion object {
        const val TAG = "UploadNewsletterTAG"
        const val REQUEST_CODE_PICK_FILE = 101
    }

    private lateinit var operations: Operations
    private lateinit var sharedPrefAdmin: SharedPrefAdmin

    private var selectedFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_newsletter)

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
        btnSelectFile.setOnClickListener {
            openFileChooser()
        }

        btnUpload.setOnClickListener {
            if (operations.checkNullOrEmpty(layoutFileName)) {
                return@setOnClickListener
            }

            val fileName = operations.getValue(layoutFileName)
            uploadFile(fileName)
        }
    }

    private fun openFileChooser() {
        Intent(Intent.ACTION_GET_CONTENT).also {
            it.type = "*/*"
            it.addCategory(Intent.CATEGORY_OPENABLE)
            try {
                startActivityForResult(it, REQUEST_CODE_PICK_FILE)
            } catch (e: ActivityNotFoundException) {
                Log.d(TAG, e.message.toString())
            }
        }
    }

    private fun uploadFile(fileName: String) {
        if (selectedFileUri == null) {
            operations.displayToast(getString(R.string.file_not_selected))
            return
        }

        val parcelFileDescriptor =
            contentResolver.openFileDescriptor(selectedFileUri!!, "r", null) ?: return

        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(cacheDir, contentResolver.getFileName(selectedFileUri!!))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        operations.showProgressDialog()
        val body = UploadRequestBody(file, "*")

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.uploadNewsletter(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                body
            ),
            RequestBody.create(
                "multipart/form-data".toMediaTypeOrNull(),
                sharedPrefAdmin.getId()!!
            ),
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), fileName)
        )

        call.enqueue(object : Callback<UploadResponse> {
            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                Log.d("onFailure", t.toString())
                operations.hideProgressDialog()
            }

            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                if (response.isSuccessful) {
                    val mResponse = response.body()
                    when (mResponse!!.error) {
                        false -> {
                            operations.displayToast(getString(R.string.updated_successfully))
                            selectedFileUri = null
                            finish()
                        }
                        true -> {
                            operations.displayToast(mResponse.message)
                            Log.d(TAG, mResponse.message)
                        }
                    }
                    operations.hideProgressDialog()
                } else {
                    operations.displayToast(getString(R.string.response_failed))
                }
            }
        })
    }

    private fun ContentResolver.getFileName(fileUri: Uri): String {
        var fileName = ""
        val cursor = this.query(fileUri, null, null, null, null)
        if (cursor != null) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            fileName = cursor.getString(nameIndex)
            cursor.close()
        }
        return fileName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PICK_FILE -> {
                    selectedFileUri = data?.data
                    Log.d(TAG, contentResolver.getFileName(selectedFileUri!!))
                }
            }
        }
    }
}