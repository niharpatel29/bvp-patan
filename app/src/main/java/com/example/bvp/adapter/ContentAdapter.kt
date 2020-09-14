package com.example.bvp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bvp.R
import com.example.bvp.model.ListItemUserDetails

class ContentAdapter(
    private val context: Context,
    private var contentList: ArrayList<ListItemUserDetails>
) : RecyclerView.Adapter<ContentAdapter.ViewHolder>() {

    companion object {
        const val TAG = "ContentAdapterTAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_user_info, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = contentList[position]

        holder.image.setImageResource(content.image)
        holder.tvHead.text = content.label
        holder.tvContent.text = content.content
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById(R.id.imgContent) as ImageView
        val tvHead = itemView.findViewById(R.id.tvLabel) as TextView
        val tvContent = itemView.findViewById(R.id.tvContent) as TextView
    }
}
