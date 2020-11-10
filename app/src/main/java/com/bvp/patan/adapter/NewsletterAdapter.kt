package com.bvp.patan.adapter

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUriExposedException
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bvp.patan.BuildConfig
import com.bvp.patan.R
import com.bvp.patan.activities.categories.Newsletters
import com.bvp.patan.api.APIInterface
import com.bvp.patan.api.postClient
import com.bvp.patan.model.ListItemNewsletter
import com.bvp.patan.operations.Operations
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*

class NewsletterAdapter(
    private val context: Context,
    private var newsletterList: ArrayList<ListItemNewsletter>
) : RecyclerView.Adapter<NewsletterAdapter.ViewHolder>() {

    companion object {
        const val TAG = "NewsletterAdapterTAG"
    }

    val operations = Operations(context)
    private val filePath =
        File(Environment.getExternalStorageDirectory().absolutePath.toString() + operations.bvpDirectory)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.list_item_newsletter, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return newsletterList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = newsletterList[position]

        holder.tvNewsletterName.text = item.fileName
        holder.tvUploadTIme.text = item.uploadTime
        setDownloadState(holder, item)

        holder.itemView.setOnClickListener {
            downloadNewsletter(item.fileName, item.type)
        }
    }

    private fun setDownloadState(holder: ViewHolder, item: ListItemNewsletter) {
        val fileName = "${item.fileName}.${item.type}"
        val file = File(filePath, fileName)
        if (file.exists()) {
            holder.imgDownloaded.visibility = View.VISIBLE
            holder.imgDownload.visibility = View.GONE
        } else {
            holder.imgDownloaded.visibility = View.GONE
            holder.imgDownload.visibility = View.VISIBLE
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNewsletterName = itemView.findViewById(R.id.tvNewsletterName) as TextView
        val tvUploadTIme = itemView.findViewById(R.id.tvUploadTime) as TextView
        val imgDownload = itemView.findViewById(R.id.imgDownload) as ImageView
        val imgDownloaded = itemView.findViewById(R.id.imgDownloaded) as ImageView
    }

    private fun downloadNewsletter(name: String?, type: String) {
        val fileName = "$name.$type"
        val file = File(filePath, fileName)
        if (file.exists()) {
            Log.d(TAG, "File already exist")
            openFile(name, type)
            return
        }

        requestStoragePermission()

        operations.showProgressDialog()

        val baseURL = postClient()!!.baseUrl()
        val fileURL = "FileUploader/Uploads/Newsletters/$fileName"
        Log.d(TAG, "$baseURL$fileURL")

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.downloadFileByUrl(fileURL)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("onFailure", t.toString())
                operations.hideProgressDialog()
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    operations.displayToast("Downloaded successfully")
                    Log.d(TAG, "Got the body for the file")
                    saveDownloadedFile(response.body()!!, name, type)
                } else {
                    operations.displayToast("Downloaded failed")
                    Log.d(TAG, "Download failed" + response.errorBody())
                }
                operations.hideProgressDialog()
            }
        })
    }

    private fun requestStoragePermission() {
        Dexter
            .withContext(context)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        Log.d(Newsletters.TAG, "Permission granted")
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
        val alertDialog = AlertDialog.Builder(context)
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
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        (context as Activity).startActivityForResult(
            intent,
            Newsletters.REQUEST_CODE_PERMISSION_SETTING
        )
    }

    private fun saveDownloadedFile(body: ResponseBody, name: String?, type: String) {
        val fileName = "$name.$type"
        try {
            if (!filePath.exists()) {
                filePath.mkdir()
            }

            val file = File(filePath, fileName)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                val data = ByteArray(4096)
                var count: Int
                val fileSize = body.contentLength()
                Log.d(TAG, "File Size: $fileSize")

                while (inputStream.read(data).also { count = it } != -1) {
                    outputStream.write(data, 0, count)
                }

                outputStream.flush()
                Log.d(TAG, file.parent!!)

                // file saved to storage
                notifyDataSetChanged()
                openFile(name, type)
                return
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "Failed to save file: ${e.message}")
                return
            } finally {
                inputStream!!.close()
                try {
                    outputStream!!.close()
                } catch (e: NullPointerException) {
                    Log.e(TAG, "null error: ${e.message}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "Failed to save the file!")
            return
        }
    }

    private fun openFile(name: String?, type: String) {
        val fileName = "$name.$type"

        val file = File(filePath, fileName)
        Log.d(TAG, file.absolutePath.toString())

        if (file.exists()) {
            val path = if (Build.VERSION.SDK_INT >= 24) {
                FileProvider.getUriForFile(
                    Objects.requireNonNull(context),
                    BuildConfig.APPLICATION_ID + ".provider", file
                )
            } else {
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW).also {
                if (type == "pdf") {
                    it.setDataAndType(path, "application/pdf")
                }
                if (type == "jpg" || type == "png" || type == "jpeg") {
                    it.setDataAndType(path, "image/*")
                }
                it.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                operations.displayToast("No compatible application found")
            } catch (e: FileUriExposedException) {
                operations.displayToast("File URI Exposed")
                Log.e(TAG, e.message.toString())
            }
        } else {
            operations.displayToast("File not exist")
        }
    }
}