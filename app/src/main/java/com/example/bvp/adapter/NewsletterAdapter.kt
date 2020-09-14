package com.example.bvp.adapter

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUriExposedException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.bvp.BuildConfig
import com.example.bvp.R
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.model.ListItemNewsletter
import com.example.bvp.operations.Operations
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

    fun updateList(list: ArrayList<ListItemNewsletter>) {
        newsletterList = list
        notifyDataSetChanged()
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
                    operations.displayToast("null error")
                    Log.e(TAG, e.message.toString())
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