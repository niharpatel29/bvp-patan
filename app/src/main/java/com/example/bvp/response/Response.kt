package com.example.bvp.response

import com.google.gson.annotations.SerializedName

data class GeneralResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("response")
    val response: String
)

data class UserLogin(
    @SerializedName("message")
    val message: String,
    @SerializedName("response")
    val response: String,
    @SerializedName("user")
    val user: UserInfo
)

data class AllUsers(
    @SerializedName("response")
    val response: String,
    @SerializedName("users")
    val users: List<UserInfo>
)

data class UserInfo(
    @SerializedName("anniversary")
    val anniversary: String,
    @SerializedName("bloodgroup")
    val bloodgroup: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("country")
    val country: String,
    @SerializedName("dob")
    val dob: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("middle_name")
    val middleName: String,
    @SerializedName("mobile_primary")
    val mobilePrimary: String,
    @SerializedName("mobile_secondary")
    val mobileSecondary: String,
    @SerializedName("position")
    val position: String,
    @SerializedName("residential_address")
    val residentialAddress: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("zipcode")
    val zipcode: String
)

data class UploadResponse(
    @SerializedName("error")
    val error: Boolean,
    @SerializedName("file")
    val `file`: String,
    @SerializedName("message")
    val message: String
)