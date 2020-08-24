package com.example.bvp.adapter

import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bvp.R
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.model.NewsletterListItem
import com.example.bvp.operations.Operations
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class NewsletterAdapter(
    private val context: Context,
    private var newsletterList: ArrayList<NewsletterListItem>
) : RecyclerView.Adapter<NewsletterAdapter.ViewHolder>() {

    companion object {
        const val TAG = "NewsletterAdapterTAG"
    }

    val operations = Operations(context)
    private val filePath =
        File(Environment.getExternalStorageDirectory().absolutePath.toString() + "/BVP Patan/")

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

        holder.itemView.setOnClickListener {
            val fileName = "${item.fileName}.${item.type}"
            downloadNewsletter(fileName)
        }
    }

    fun updateList(list: ArrayList<NewsletterListItem>) {
        newsletterList = list
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNewsletterName = itemView.findViewById(R.id.tvNewsletterName) as TextView
        val tvUploadTIme = itemView.findViewById(R.id.tvUploadTime) as TextView
    }

    private fun downloadNewsletter(fileName: String) {
        if (File(filePath, fileName).exists()) {
            operations.displayToast("already exist")
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
                    saveDownloadedFile(response.body()!!, fileName)
                } else {
                    operations.displayToast("Downloaded failed")
                    Log.d(TAG, "Download failed" + response.errorBody())
                }
                operations.hideProgressDialog()
            }
        })
    }

    private fun saveDownloadedFile(body: ResponseBody, fileName: String) {
        try {
            val filePath =
                File(Environment.getExternalStorageDirectory().absolutePath.toString() + "/BVP Patan/")
            if (!filePath.exists()) {
                Log.d(TAG, "path not exist")
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
}