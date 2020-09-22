package com.bvp.patan.api

import com.bvp.patan.admin.other.AdminGeneralResponse
import com.bvp.patan.admin.other.AdminLoginModel
import com.bvp.patan.admin.other.MakeAnnouncementResponse
import com.bvp.patan.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface APIInterface {
    //default_image login
    @POST("Login.php")
    @FormUrlEncoded
    fun performUserLogin(
        @Field("mobile_primary") mobile_primary: String,
        @Field("password") password: String? = null
    ): Call<UserLogin>

    //signup default_image
    @POST("Signup.php")
    @FormUrlEncoded
    fun performUserSignup(
        @Field("mobile_primary") mobile_primary: String,
        @Field("first_name") first_name: String,
        @Field("middle_name") middle_name: String,
        @Field("last_name") last_name: String,
        @Field("password") password: String
    ): Call<GeneralResponse>

    //modify default_image details
    @POST("ModifyProfile.php")
    @FormUrlEncoded
    fun performUpdateUserProfile(
        @Field("user_id") user_id: String?,
        @Field("mobile_primary") mobile_primary: String?,
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
        @Field("residential_address") residential_address: String?,
        @Field("position") position: String?
    ): Call<GeneralResponse>

    //get all users
    @POST("GetAllUsers.php")
    @FormUrlEncoded
    fun performGetAllUsers(
        @Field("user_id") user_id: String?,
        @Field("mobile_primary") mobile_primary: String?
    ): Call<AllUsers>

    @POST("GetNewsletters.php")
    @FormUrlEncoded
    fun performGetNewsletters(
        @Field("user_id") user_id: String?,
        @Field("mobile_primary") mobile_primary: String?
    ): Call<GetNewsletters>

    @POST("GetAnnouncement.php")
    @FormUrlEncoded
    fun performGetAnnouncement(
        @Field("topic") topic: String?
    ): Call<GetAnnouncement>

    @POST("GetPhotosLinks.php")
    @FormUrlEncoded
    fun performGetPhotosLinks(
        @Field("user_id") user_id: String?,
        @Field("mobile_primary") mobile_primary: String?
    ): Call<GetPhotosLinks>

    //check_user_exist admin
    @POST("Admin/AdminLogin.php")
    @FormUrlEncoded
    fun performAdminLogin(
        @Field("admin_username") admin_username: String,
        @Field("admin_password") admin_password: String
    ): Call<AdminLoginModel>

    @POST("SendFeedback.php")
    @FormUrlEncoded
    fun performSendFeedback(
        @Field("user_id") user_id: String?,
        @Field("rating") rating: String,
        @Field("feedback") feedback: String,
        @Field("brand") brand: String,
        @Field("model") model: String,
        @Field("device") device: String,
        @Field("version") version: String
    ): Call<GeneralResponse>

    //admin registers new default_image
    @POST("Admin/AdminRegistersNewUser.php")
    @FormUrlEncoded
    fun performAdminRegistersNewUser(
        @Field("admin_id") admin_id: String?,
        @Field("first_name") first_name: String,
        @Field("last_name") last_name: String,
        @Field("category") category: String,
        @Field("position") position: String,
        @Field("mobile_primary") mobile_primary: String
    ): Call<AdminGeneralResponse>

    //admin registers new default_image
    @POST("Admin/MakeAnnouncement.php?apicall=new")
    @FormUrlEncoded
    fun performMakeAnnouncement(
        @Field("admin_id") admin_id: String?,
        @Field("topic") topic: String,
        @Field("title") title: String,
        @Field("message") message: String
    ): Call<MakeAnnouncementResponse>

    @POST("Admin/UploadPhotosLink.php")
    @FormUrlEncoded
    fun performUploadPhotosLink(
        @Field("admin_id") admin_id: String?,
        @Field("link") link: String,
        @Field("description") description: String
    ): Call<AdminGeneralResponse>

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

    @Streaming
    @GET
    fun downloadFileByUrl(@Url fileName: String): Call<ResponseBody>
}
