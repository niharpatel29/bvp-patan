package com.example.bvp.admin

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bvp.R
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.operations.Operations
import com.example.bvp.response.UploadResponse
import com.example.bvp.upload.UploadRequestBody
import kotlinx.android.synthetic.main.upload_newsletter.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UploadNewsletter : AppCompatActivity() {

    companion object {
        const val TAG = "UploadNewsletterTAG"
        const val REQUEST_CODE_PICK_FILE = 101
    }

    private lateinit var operations: Operations

    private var selectedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_newsletter)

        operations = Operations(this)

        handleButtonClicks()
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
        if (selectedFile == null) {
            operations.displayToast(getString(R.string.select_file_first))
            return
        }

        operations.showProgressDialog()
        val body = UploadRequestBody(selectedFile, "*")

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.uploadNewsletter(
            MultipartBody.Part.createFormData(
                "file",
                selectedFile!!.name,
                body
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
                            selectedFile = null
                            finish()
                        }
                        true -> {
                            operations.displayToast(mResponse.message)
                        }
                    }
                    operations.hideProgressDialog()
                } else {
                    operations.displayToast(getString(R.string.response_failed))
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PICK_FILE -> {
                    val resultUri = data?.data
                    selectedFile = File(resultUri!!.path!!)
                }
            }
        }
    }
}