package com.bvp.patan.adapter

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
import com.bvp.patan.R
import com.bvp.patan.UserDetails
import com.bvp.patan.api.postClient
import com.bvp.patan.model.ListItemUsers
import com.bvp.patan.other.CircleTransform

class UsersAdapter(
    private val context: Context,
    private var userList: ArrayList<ListItemUsers>
) : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        val imageURL =
            "${postClient()!!.baseUrl()}FileUploader/Uploads/ProfilePictures/${user.userId}.jpg"

        Glide
            .with(context)
            .load(imageURL)
            .error(R.drawable.default_image)
            .placeholder(R.drawable.default_image)
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

    fun updateList(list: ArrayList<ListItemUsers>) {
        userList = list
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById(R.id.imgProfile) as ImageView
        val tvName = itemView.findViewById(R.id.tvName) as TextView
        val tvPosition = itemView.findViewById(R.id.tvPosition) as TextView
    }
}
