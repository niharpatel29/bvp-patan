package com.bvp.patan.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bvp.patan.R
import com.bvp.patan.admin.other.AdminGeneralResponse
import com.bvp.patan.admin.other.SharedPrefAdmin
import com.bvp.patan.api.APIInterface
import com.bvp.patan.api.postClient
import com.bvp.patan.model.ListItemUsers
import com.bvp.patan.operations.displayToast
import com.bvp.patan.operations.hideProgressDialog
import com.bvp.patan.operations.showProgressDialog
import com.bvp.patan.other.CircleTransform
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MakeAdminAdapter(
    private val context: Context,
    private var userList: ArrayList<ListItemUsers>
) : RecyclerView.Adapter<MakeAdminAdapter.ViewHolder>() {

    lateinit var mView: View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_user, parent, false)
        mView = view
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
            confirmMakeAdmin(user.userId)
        }
    }

    private fun confirmMakeAdmin(userId: String?) {
        val builder = AlertDialog.Builder(context)

        builder
            .setTitle(context.getString(R.string.confirmation))
            .setMessage(context.getString(R.string.confirm_make_admin))
            .setCancelable(true)

        builder
            .setPositiveButton(context.getString(R.string.make_admin)) { Dialog, id ->
                Dialog.dismiss()
                makeNewAdmin(userId)
            }

        builder
            .setNegativeButton(context.getString(R.string.cancel)) { Dialog, id ->
                Dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun makeNewAdmin(userId: String?) {
        context.showProgressDialog()

        val adminMadeBy = SharedPrefAdmin(context).getId()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performMakeNewAdmin(userId, adminMadeBy)

        call.enqueue(object : Callback<AdminGeneralResponse> {
            override fun onFailure(call: Call<AdminGeneralResponse>, t: Throwable) {
                hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<AdminGeneralResponse>,
                response: Response<AdminGeneralResponse>
            ) {
                if (response.isSuccessful) {
                    val responseMessage = response.body()!!.message
                    when (response.body()!!.response) {
                        "ok" -> {
                            context.displayToast(responseMessage)
                        }
                        "exist" -> {
                            context.displayToast(responseMessage)
                        }
                        "error" -> {
                            context.displayToast(context.getString(R.string.sql_error))
                        }
                        else -> {
                            context.displayToast(context.getString(R.string.unknown_error))
                        }
                    }
                } else {
                    context.displayToast(context.getString(R.string.response_failed))
                }
                hideProgressDialog()
            }
        })
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