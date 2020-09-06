package com.example.bvp.api

import com.example.bvp.admin.other.AdminLoginModel
import com.example.bvp.admin.other.AdminRegistersNewUserModel
import com.example.bvp.admin.other.MakeAnnouncementResponse
import com.example.bvp.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface APIInterface {
    //default_image login
    @POST("userLogin.php")
    @FormUrlEncoded
    fun performUserLogin(
        @Field("mobile_primary") mobile_primary: String,
        @Field("password") password: String? = null
    ): Call<UserLogin>

    //signup default_image
    @POST("userSignup.php")
    @FormUrlEncoded
    fun performUserSignup(
        @Field("mobile_primary") mobile_primary: String,
        @Field("first_name") first_name: String,
        @Field("middle_name") middle_name: String,
        @Field("last_name") last_name: String,
        @Field("password") password: String
    ): Call<GeneralResponse>

    //modify default_image details
    @POST("editProfile.php")
    @FormUrlEncoded
    fun performUpdateUserProfile(
        @Field("user_id") user_id: String?,
        @Field("first_name") first_name: String?,
        @Field("middle_name") middle_name: String?,
        @Field("last_name") last_name: String?,
        @Field("mobile_secondary") mobile_secondary: String?,
        @Field("email") email: String?,
        @Field("dob") dob: String?,
        @Field("anniversary") anniversary: String?,
        @Field("bloodgroup") bloodgroup: String?,
        @Field("gender") gender: String?,
        @Field("country") country: String?,
        @Field("state") state: String?,
        @Field("city") city: String?,
        @Field("zipcode") zipcode: String?,
        @Field("residential_address") residential_address: String?
    ): Call<GeneralResponse>

    //get all users
    @POST("getAllUsers.php")
    @FormUrlEncoded
    fun performGetAllUsers(
        @Field("mobile_primary") mobile_primary: String,
        @Field("password") password: String
    ): Call<AllUsers>

    //check_user_exist admin
    @POST("admin/adminLogin.php")
    @FormUrlEncoded
    fun performAdminLogin(
        @Field("admin_username") admin_username: String,
        @Field("admin_password") admin_password: String
    ): Call<AdminLoginModel>

    //admin registers new default_image
    @POST("admin/adminRegistersNewUser.php")
    @FormUrlEncoded
    fun performAdminRegistersNewUser(
        @Field("admin_id") admin_id: String?,
        @Field("first_name") first_name: String,
        @Field("last_name") last_name: String,
        @Field("category") category: String,
        @Field("position") position: String,
        @Field("mobile_primary") mobile_primary: String
    ): Call<AdminRegistersNewUserModel>

    //admin registers new default_image
    @POST("admin/makeAnnouncement.php?apicall=new")
    @FormUrlEncoded
    fun performMakeAnnouncement(
        @Field("admin_id") admin_id: String?,
        @Field("topic") topic: String,
        @Field("title") title: String,
        @Field("message") message: String
    ): Call<MakeAnnouncementResponse>

    @POST("getAnnouncement.php")
    fun performGetAnnouncement(): Call<GetAnnouncement>

    @Multipart
    @POST("FileUploader/API.php?apicall=profile_picture")
    fun uploadProfilePicture(
        @Part image: MultipartBody.Part,
        @Part("user_id") userId: RequestBody
    ): Call<UploadResponse>

    @Multipart
    @POST("FileUploader/API.php?apicall=newsletter")
    fun uploadNewsletter(
        @Part file: MultipartBody.Part,
        @Part("admin_id") adminId: RequestBody,
        @Part("file_name") fileName: RequestBody
    ): Call<UploadResponse>

    @POST("getNewsletters.php")
    fun performGetNewsletters(): Call<GetNewsletters>

    @Streaming
    @GET
    fun downloadFileByUrl(@Url fileName: String): Call<ResponseBody>
}
