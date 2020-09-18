package com.example.bvp.admin.other

import com.google.gson.annotations.SerializedName

data class AdminLoginModel(
    @SerializedName("admin")
    val admin: Admin,
    @SerializedName("message")
    val message: String,
    @SerializedName("response")
    val response: String
)
//
data class Admin(
    @SerializedName("admin_id")
    val adminId: String,
    @SerializedName("admin_name")
    val adminName: String
)

data class AdminGeneralResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("response")
    val response: String
)

data class MakeAnnouncementResponse(
    @SerializedName("message_id")
    val messageId: String,
    @SerializedName("response")
    val response: String,
    @SerializedName("response_message")
    val responseMessage: String
)