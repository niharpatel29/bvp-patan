package com.bvp.patan

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bvp.patan.activities.categories.*
import com.bvp.patan.admin.AdminLogin
import com.bvp.patan.api.APIInterface
import com.bvp.patan.api.postClient
import com.bvp.patan.firebase.Topic
import com.bvp.patan.model.UserModel
import com.bvp.patan.operations.ImageOperations
import com.bvp.patan.operations.Operations
import com.bvp.patan.prefs.SharedPref
import com.bvp.patan.response.AllUsers
import com.bvp.patan.sqlite.MyDBHandler
import kotlinx.android.synthetic.main.categories.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class Categories : AppCompatActivity() {

    companion object {
        const val TAG = "CategoryTAG"
    }

    private lateinit var sharedPref: SharedPref
    private lateinit var dbHandler: MyDBHandler
    private lateinit var operations: Operations
    private lateinit var imageOperations: ImageOperations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.categories)

        sharedPref = SharedPref(this)
        dbHandler = MyDBHandler(this)
        operations = Operations(this)
        imageOperations = ImageOperations(this)

        invalidateOptionsMenu()
        toolbar()
        handleButtonClicks()
        createDirectory()
        downloadProfilePicture()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        menu!!.findItem(R.id.action_admin_panel).isVisible = adminButtonVisibility()
        return true
    }

    private fun adminButtonVisibility(): Boolean {
        return sharedPref.getAdminFlag()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, Profile::class.java))
                true
            }
            R.id.action_admin_panel -> {
                startActivity(Intent(this, AdminLogin::class.java))
                true
            }
            R.id.action_refresh -> {
                getAllUsersFromServer()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_test -> {
                crash()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun crash() {
//        throw RuntimeException("Test Crash")
        operations.displayToast("Runtime Exception")
    }

    private fun toolbar() {
        setSupportActionBar(toolbar)
        toolbar.navigationIcon?.setColorFilter(
            resources.getColor(R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
        toolbar.overflowIcon?.setColorFilter(
            resources.getColor(R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    private fun handleButtonClicks() {
        btnMembers.setOnClickListener {
            startActivity(Intent(this, UserDirectory::class.java))
        }
        btnNewsletters.setOnClickListener {
            startActivity(Intent(this, Newsletters::class.java))
        }
        btnAnnouncement.setOnClickListener {
            startActivity(Intent(this, Announcement::class.java))
        }
        btnPhotos.setOnClickListener {
            startActivity(Intent(this, Photos::class.java))
        }
        btnProjects.setOnClickListener {
            startActivity(Intent(this, Projects::class.java))
        }
        btnCalendarEvents.setOnClickListener {
            startActivity(Intent(this, CalendarEvents::class.java))
        }
        btnFeedback.setOnClickListener {
            startActivity(Intent(this, Feedback::class.java))
        }
        btnAbout.setOnClickListener {
            startActivity(Intent(this, About::class.java))
        }
    }

    private fun getAllUsersFromServer() {
        operations.showProgressDialog()

        val mUserId = sharedPref.getId()
        val userMobile = sharedPref.getMobilePrimary()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performGetAllUsers(mUserId, userMobile)

        call.enqueue(object : Callback<AllUsers> {
            override fun onFailure(call: Call<AllUsers>, t: Throwable) {
                operations.hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<AllUsers>,
                response: Response<AllUsers>
            ) {
                if (response.isSuccessful) {
                    when (response.body()!!.response) {
                        "ok" -> {
                            val user = response.body()!!.users

                            for (i in user.indices) {
                                val userId = user[i].userId
                                val mobilePrimary = user[i].mobilePrimary
                                val firstName = user[i].firstName
                                val middleName = user[i].middleName
                                val lastName = user[i].lastName
                                val mobileSecondary = user[i].mobileSecondary
                                val email = user[i].email
                                val dob = user[i].dob
                                val anniversary = user[i].anniversary
                                val bloodgroup = user[i].bloodgroup
                                val gender = user[i].gender
                                val country = user[i].country
                                val state = user[i].state
                                val city = user[i].city
                                val zipcode = user[i].zipcode
                                val residentialAddress = user[i].residentialAddress
                                val position = user[i].position

                                val mUser = UserModel(
                                    userId,
                                    mobilePrimary,
                                    firstName,
                                    middleName,
                                    lastName,
                                    mobileSecondary,
                                    email,
                                    dob,
                                    anniversary,
                                    bloodgroup,
                                    gender,
                                    country,
                                    state,
                                    city,
                                    zipcode,
                                    residentialAddress,
                                    position
                                )

                                if (dbHandler.isUserExist(mUser.userId)) {
                                    Log.d(TAG, "exist: ${mUser.userId}")
                                    dbHandler.updateUserDetails(mUser)
                                } else {
                                    Log.d(TAG, "notExist: ${mUser.userId}")
                                    dbHandler.addUser(mUser)
                                }
                            }
                            operations.displayToast(getString(R.string.refresh_complete))
                        }
                        "failed" -> {
                            operations.displayToast(getString(R.string.please_login_again))
                        }
                        else -> {
                            operations.displayToast(getString(R.string.unknown_error))
                        }
                    }
                } else {
                    operations.displayToast(getString(R.string.response_failed))
                }
                operations.hideProgressDialog()
            }
        })
    }

    private fun createDirectory() {
        val filePath =
            File(Environment.getExternalStorageDirectory().absolutePath.toString() + operations.bvpDirectory)
        if (!filePath.exists()) {
            filePath.mkdir()
        }
    }

    private fun downloadProfilePicture() {
        val mFile = File(imageOperations.profilePicturePath, imageOperations.fileName)
        if (mFile.exists()) {
            Log.d(TAG, "Already exist")
            return
        }

        val baseUrl = postClient()!!.baseUrl().toString()
        val file = "FileUploader/Uploads/ProfilePictures/${sharedPref.getId()}.jpg"
        val url = "$baseUrl$file"
        Log.d(TAG, url)

        Glide
            .with(this)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.d(TAG, "error loading file")
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    imageOperations.saveToInternalStorage(resource)
                }
            })
    }

    private fun logout() {
        handleSubscription()
        sharedPref.userLogout()
        dbHandler.clearDatabase()
        deleteProfilePicture()

        startActivity(Intent(this, Login::class.java))
        finish()
    }

    private fun handleSubscription() {
        Topic(this).run {
            unsubscribe(login)
            unsubscribe(sharedPref.getCategory()!!)
        }
    }

    private fun deleteProfilePicture() {
        val file = File(imageOperations.profilePicturePath, imageOperations.fileName)
        if (file.exists()) {
            file.delete()
        }
    }
}