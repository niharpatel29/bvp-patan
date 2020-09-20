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
import com.bvp.patan.model.ListItemCalendar
import com.bvp.patan.other.CircleTransform
import java.util.*

class CalendarAdapter(
    private val context: Context,
    private var userList: ArrayList<ListItemCalendar>
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.list_item_calendar_events, parent, false)
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
        holder.tvDOB.text = user.date

        setIcon(holder, user.type)

        holder.itemView.setOnClickListener {
            context.startActivity(
                Intent(context, UserDetails::class.java)
                    .putExtra("user_id", user.userId)
            )
        }
    }

    private fun setIcon(holder: ViewHolder, type: String?) {
        if (type == context.getString(R.string.type_birthday)) {
            holder.imgCake.visibility = View.VISIBLE
            holder.imgCouple.visibility = View.GONE
        } else {
            holder.imgCake.visibility = View.GONE
            holder.imgCouple.visibility = View.VISIBLE
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById(R.id.imgProfile) as ImageView
        val tvName = itemView.findViewById(R.id.tvName) as TextView
        val tvDOB = itemView.findViewById(R.id.tvDOB) as TextView
        val imgCake = itemView.findViewById(R.id.imgCake) as ImageView
        val imgCouple = itemView.findViewById(R.id.imgCouple) as ImageView
    }
}
