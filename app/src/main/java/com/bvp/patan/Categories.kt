package com.bvp.patan

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
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
import com.bvp.patan.model.UserModel
import com.bvp.patan.operations.ImageOperations
import com.bvp.patan.operations.Operations
import com.bvp.patan.operations.internetAvailable
import com.bvp.patan.operations.logout
import com.bvp.patan.prefs.SharedPref
import com.bvp.patan.response.AllUsers
import com.bvp.patan.sqlite.MyDBHandler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
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
        initialCalls()
        handleButtonClicks()
        createDirectory()
        downloadProfilePicture()
        checkForAppUpdate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        menu!!.findItem(R.id.action_admin_panel).isVisible = adminButtonVisibility()
        return true
    }

    private fun checkForAppUpdate() {
        if (!internetAvailable()) {
            Log.e(TAG, "No internet")
            return
        }

        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                showUpdateDialog()
            }
        }
    }

    private fun showUpdateDialog() {
        val dialog = MaterialAlertDialogBuilder(this)

        dialog
            .setTitle("Update available")
            .setMessage("Please update app to continue")
            .setCancelable(true)

        dialog.setPositiveButton(getString(R.string.update)) { Dialog, id ->
            redirectToPlayStore()
            Dialog.dismiss()
        }

        dialog.setNegativeButton(getString(R.string.cancel)) { Dialog, id ->
            Dialog.cancel()
        }

        dialog.setOnCancelListener {
            finishAffinity()
        }

        dialog.create().show()
    }

    private fun redirectToPlayStore() {
        val appPackageName = packageName

        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, e.message.toString())
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
        /*try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                this,
                CheckForUpdate.REQUEST_CODE_APP_UPDATE
            )
        } catch (e: IntentSender.SendIntentException) {
            skipIfLoggedIn()
            Log.d(TAG, e.message.toString())
        }*/
    }

    private fun initialCalls() {
        if (!dbHandler.isColumnExist()) {
            dbHandler.addColumnIfNotExist()
            refreshUserList()
        }
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
                refreshUserList()
                true
            }
            R.id.action_logout -> {
                logout()
                startActivity(Intent(this, Login::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    private fun refreshUserList() {
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
                            dbHandler.clearDatabase()

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
                                val category = user[i].category

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
                                    position,
                                    category
                                )

                                Log.d(TAG, "notExist: ${mUser.userId}")
                                dbHandler.addUser(mUser)

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
}