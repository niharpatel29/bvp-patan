package com.bvp.patan.model

import android.content.Context
import com.bvp.patan.R

data class ChannelContainer(
    val context: Context,
    val id: String = context.getString(R.string.channel_default_id),
    val name: String = context.getString(R.string.channel_default_name)
)