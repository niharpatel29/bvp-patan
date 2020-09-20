package com.bvp.patan.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bvp.patan.R
import com.bvp.patan.model.ListItemPhotos
import com.bvp.patan.operations.Operations
import java.util.*

class PhotosAdapter(
    private val context: Context,
    private var photosList: ArrayList<ListItemPhotos>
) : RecyclerView.Adapter<PhotosAdapter.ViewHolder>() {

    companion object {
        const val TAG = "PhotosAdapterTAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.list_item_photos, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return photosList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = photosList[position]

        holder.tvLink.text = item.link
        holder.tvDesc.text = item.desc

        holder.tvLink.setOnLongClickListener {
            copyToClipboard(item.link)
            true
        }

        holder.itemView.setOnLongClickListener {
            copyToClipboard(item.link)
            true
        }
    }

    private fun copyToClipboard(link: String?) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipData.newPlainText("copy", link)
        clipboardManager.setPrimaryClip(clipData)

        Operations(context).displayToast(context.getString(R.string.link_copied_to_clipboard))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLink = itemView.findViewById(R.id.tvLink) as TextView
        val tvDesc = itemView.findViewById(R.id.tvDesc) as TextView
    }
}