package com.example.bvp.model

import android.content.Context
import com.example.bvp.R

data class ChannelContainer(
    val context: Context,
    val id: String = context.getString(R.string.channel_default_id),
    val name: String = context.getString(R.string.channel_default_name)
)