package com.bvp.patan.response

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
    @SerializedName("category")
    val category: String,
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

data class GetNewsletters(
    @SerializedName("newsletter")
    val newsletter: List<Newsletter>,
    @SerializedName("response")
    val response: String
)

data class Newsletter(
    @SerializedName("file_name")
    val fileName: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("path")
    val path: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("upload_time")
    val uploadTime: String
)

data class GetAnnouncement(
    @SerializedName("id")
    val id: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("response")
    val response: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("title")
    val title: String
)

data class GetPhotosLinks(
    @SerializedName("photos")
    val photos: List<Photo>,
    @SerializedName("response")
    val response: String
)

data class Photo(
    @SerializedName("description")
    val description: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("link")
    val link: String,
    @SerializedName("time")
    val time: String
)