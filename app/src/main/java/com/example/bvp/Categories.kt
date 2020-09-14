package com.example.bvp

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
import com.example.bvp.activities.categories.*
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.firebase.Topic
import com.example.bvp.model.UserModel
import com.example.bvp.operations.ImageOperations
import com.example.bvp.operations.Operations
import com.example.bvp.prefs.SharedPref
import com.example.bvp.response.AllUsers
import com.example.bvp.sqlite.MyDBHandler
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

        toolbar()
        handleButtonClicks()
        createDirectory()
        downloadProfilePicture()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, Profile::class.java))
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_refresh -> {
                getAllUsersFromServer("7698159590", "29111997")
                true
            }
            R.id.action_test -> {
//                logout()
                abc()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun abc() {
        for (i in dbHandler.checkBirthdayToday().indices) {
            Log.d(TAG, "name: ${dbHandler.checkBirthdayToday()[i].userId}")
            Log.d(TAG, "name: ${dbHandler.checkBirthdayToday()[i].name}")
            Log.d(TAG, "name: ${dbHandler.checkBirthdayToday()[i].date}")
        }
        /*val stringToDate = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val bDate = stringToDate.parse("10-Sep-2020")!!

        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("dd-MMM", Locale.getDefault())
        val systemDate = simpleDateFormat.format(calendar.time)

        if (simpleDateFormat.format(bDate).contains(systemDate, true)) {
            operations.displayToast("match")
        }

        Log.d(TAG, systemDate)
        Log.d(TAG, simpleDateFormat.format(bDate))*/
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

        btnCalendarEvents.setOnClickListener {
            startActivity(Intent(this, CalendarEvents::class.java))
        }

        btnFeedback.setOnClickListener {
            startActivity(Intent(this, Feedback::class.java))
        }
    }

    private fun getAllUsersFromServer(userMobile: String, userPassword: String) {
        operations.showProgressDialog()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performGetAllUsers(userMobile, userPassword)

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
                                dbHandler.updateUserDetails(mUser)
                            }
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