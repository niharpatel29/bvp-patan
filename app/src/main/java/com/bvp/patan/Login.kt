package com.bvp.patan

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bvp.patan.api.APIInterface
import com.bvp.patan.api.postClient
import com.bvp.patan.background_worker.RegisterAlarmBroadcast
import com.bvp.patan.broadcast.MyReceiver
import com.bvp.patan.firebase.Topic
import com.bvp.patan.model.UserModel
import com.bvp.patan.operations.*
import com.bvp.patan.prefs.SharedPref
import com.bvp.patan.response.AllUsers
import com.bvp.patan.response.UserLogin
import com.bvp.patan.sqlite.MyDBHandler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class Login : AppCompatActivity() {

    companion object {
        const val TAG = "LoginTAG"
    }

    private lateinit var sharedPref: SharedPref
    private lateinit var dbHandler: MyDBHandler
    private lateinit var imageOperations: ImageOperations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        sharedPref = SharedPref(this)
        dbHandler = MyDBHandler(this)
        imageOperations = ImageOperations(this)

        toolbar()
//        checkForUpdate()
        initialCalls()
        skipIfLoggedIn()
        handleButtonClicks()
    }

    /*private fun checkForUpdate() {
        CheckForUpdate(this).execute()
    }*/

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
            finish()
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

    private fun initialCalls() {
        Topic(this).run { subscribe(global) }
        registerBroadcast()
    }

    private fun registerBroadcast() {
        if (!sharedPref.getBroadcastRegistrationFlag()) {
            RegisterAlarmBroadcast(this).execute()
        }
    }

    private fun skipIfLoggedIn() {
        if (sharedPref.getLoginStatus()) {
            startActivity(Intent(this, Categories::class.java))
            finish()
        } else {
            checkForAppUpdate()
        }
    }

    private fun handleButtonClicks() {
        btnLogin.setOnClickListener {
            // return if empty
            if (checkNullOrEmpty(layoutMobile)) {
                return@setOnClickListener
            }
            if (isPasswordLayoutVisible()) {
                if (checkNullOrEmpty(layoutPassword)) {
                    return@setOnClickListener
                }
            }
            // check_user_exist if not empty
            val userMobile = getValue(layoutMobile)
            val userPassword = getValue(layoutPassword)

            userLogin(userMobile, userPassword)
        }
    }

    private fun isPasswordLayoutVisible(): Boolean {
        return layoutPassword.visibility == View.VISIBLE
    }

    private fun userLogin(userMobile: String, userPassword: String) {
        if (!internetAvailable()) {
            view.displaySnackBar(getString(R.string.no_internet))
            return
        }
        showProgressDialog()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = if (!isPasswordLayoutVisible()) {
            apiService.performUserLogin(userMobile)
        } else {
            apiService.performUserLogin(userMobile, userPassword)
        }

        call.enqueue(object : Callback<UserLogin> {
            override fun onFailure(call: Call<UserLogin>, t: Throwable) {
                hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<UserLogin>,
                response: Response<UserLogin>
            ) {
                if (response.isSuccessful) {
                    when (response.body()!!.response) {
                        "ok" -> {
                            val user = response.body()!!.user

                            val userId = user.userId
                            val mobilePrimary = user.mobilePrimary
                            val firstName = user.firstName
                            val middleName = user.middleName
                            val lastName = user.lastName
                            val mobileSecondary = user.mobileSecondary
                            val category = user.category
                            val position = user.position
                            val email = user.email
                            val dob = user.dob
                            val anniversary = user.anniversary
                            val bloodgroup = user.bloodgroup
                            val gender = user.gender
                            val country = user.country
                            val state = user.state
                            val city = user.city
                            val zipcode = user.zipcode
                            val residentialAddress = user.residentialAddress
                            val adminFlag = user.adminFlag

                            try {
                                sharedPref.run {
                                    setLoginStatus(true)
                                    setId(userId)
                                    setMobilePrimary(mobilePrimary)
                                    setFirstname(firstName)
                                    setMiddlename(middleName)
                                    setLastname(lastName)
                                    setMobileSecondary(mobileSecondary)
                                    setCategory(category)
                                    setPosition(position)
                                    setEmail(email)
                                    setDOB(dob)
                                    setAnniversary(anniversary)
                                    setBloodGroup(bloodgroup)
                                    setGender(gender)
                                    setCountry(country)
                                    setState(state)
                                    setCity(city)
                                    setZipcode(zipcode)
                                    setResidentialAddress(residentialAddress)
                                    setAdminFlag(adminFlag)
                                }
                            } catch (e: Exception) {
                                displayToast(e.message.toString())
                            }

                            handleSubscription()
                            getAllUsersFromServer(userId, userMobile)
                        }
                        "failed" -> {
                            hideProgressDialog()
                            displayToast(getString(R.string.login_failed))
                        }
                        "able_to_login" -> {
                            hideProgressDialog()
                            actionOnAbleToLogin()
                        }
                        "able_to_signup" -> {
                            hideProgressDialog()
                            startActivity(
                                Intent(this@Login, Signup::class.java)
                                    .putExtra("user_mobile", userMobile)
                            )
                        }
                        "user_not_exist" -> {
                            hideProgressDialog()
                            displayToast(getString(R.string.user_not_exist))
                        }
                        "error" -> {
                            hideProgressDialog()
                            displayToast(getString(R.string.sql_error))
                        }
                        else -> {
                            hideProgressDialog()
                            displayToast(getString(R.string.unknown_error))
                        }
                    }
                } else {
                    hideProgressDialog()
                    displayToast(getString(R.string.response_failed))
                }
            }
        })
    }

    private fun actionOnAbleToLogin() {
        layoutPassword.visibility = View.VISIBLE
        layoutPassword.editText?.requestFocus()
        btnLogin.text = getString(R.string.login)
    }

    private fun handleSubscription() {
        Topic(this).run {
            subscribe(login)
            subscribe(sharedPref.getCategory()!!)
        }
    }

    private fun getAllUsersFromServer(mUserId: String, userMobile: String) {
        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performGetAllUsers(mUserId, userMobile)

        call.enqueue(object : Callback<AllUsers> {
            override fun onFailure(call: Call<AllUsers>, t: Throwable) {
                hideProgressDialog()
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
                                dbHandler.addUser(mUser)
                            }
                            startActivity(Intent(this@Login, Categories::class.java))
                            finish()
                        }
                        else -> {
                            displayToast(getString(R.string.unknown_error))
                        }
                    }
                } else {
                    displayToast(getString(R.string.response_failed))
                }
                hideProgressDialog()
            }
        })
    }
}