package com.example.bvp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.bvp.R
import com.example.bvp.UserDetails
import com.example.bvp.model.ListItem
import com.example.bvp.other.CircleTransform

class CustomAdapter(private val context: Context, private var userList: ArrayList<ListItem>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    companion object {
        const val TAG = "CustomAdapterTAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        Glide
            .with(context)
            .load(user.image)
            .error(R.mipmap.ic_launcher)
            .transition(withCrossFade())
            .thumbnail(0.5f)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transform(CircleTransform(context))
            .into(holder.imageView)

        holder.tvName.text = user.name
        holder.tvPosition.text = user.position

        holder.itemView.setOnClickListener {
            context.startActivity(
                Intent(context, UserDetails::class.java)
                    .putExtra("user_id", user.userId)
            )
        }
    }

    fun updateList(list: ArrayList<ListItem>) {
        userList = list
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById(R.id.imgProfile) as ImageView
        val tvName = itemView.findViewById(R.id.tvName) as TextView
        val tvPosition = itemView.findViewById(R.id.tvPosition) as TextView
    }
}
