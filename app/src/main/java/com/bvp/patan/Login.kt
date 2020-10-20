package com.bvp.patan

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bvp.patan.api.APIInterface
import com.bvp.patan.api.postClient
import com.bvp.patan.broadcast.MyReceiver
import com.bvp.patan.firebase.Topic
import com.bvp.patan.model.UserModel
import com.bvp.patan.operations.*
import com.bvp.patan.prefs.SharedPref
import com.bvp.patan.response.AllUsers
import com.bvp.patan.response.UserLogin
import com.bvp.patan.sqlite.MyDBHandler
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class Login : AppCompatActivity() {

    companion object {
        const val TAG = "LoginTAG"
        const val REQUEST_CODE_APP_UPDATE = 111
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

        dumpDatabase()

        toolbar()
        initialCalls()
        checkLoginStatus()
        handleButtonClicks()
    }

    override fun onStart() {
        super.onStart()
        checkForAppUpdate()
    }

    private fun dumpDatabase() {
        if (BuildConfig.VERSION_NAME > "2.0.4") {
            if (!sharedPref.getDatabaseDeletedFlag()) {
                if (getDatabasePath(dbHandler.databaseName).exists()) {
                    Log.d(TAG, "exist")
                    if (deleteDatabase(dbHandler.databaseName)) {
                        sharedPref.setDatabaseDeletedFlag(true)
                        handleLogoutTopics()
                        sharedPref.userLogout()
                        deleteProfilePicture()
                        Log.d(TAG, "deleted")
                    }
                } else {
                    Log.d(TAG, "not exist")
                }
            } else {
                Log.d(TAG, "already deleted")
            }
        } else {
            Log.d(TAG, "version error")
        }
    }

    private fun handleLogoutTopics() {
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

    private fun checkForAppUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        REQUEST_CODE_APP_UPDATE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.d(TAG, e.message.toString())
                    displayToast(getString(R.string.app_update_failed))
                }
            }
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

    private fun initialCalls() {
        Topic(this).run { subscribe(global) }
        sendBroadcast()
    }

    private fun sendBroadcast() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 30)
        calendar.set(Calendar.SECOND, 0)

        Log.d(TAG, calendar.get(Calendar.YEAR).toString())
        Log.d(TAG, calendar.get(Calendar.MONTH).toString())
        Log.d(TAG, calendar.get(Calendar.DAY_OF_MONTH).toString())
        Log.d(TAG, calendar.get(Calendar.HOUR_OF_DAY).toString())
        Log.d(TAG, calendar.get(Calendar.MINUTE).toString())
        Log.d(TAG, calendar.get(Calendar.SECOND).toString())

        //creating a new intent specifying the broadcast receiver
        val intent = Intent(applicationContext, MyReceiver::class.java)
        //creating a pending intent using the intent
        val pendingIntent =
            PendingIntent.getBroadcast(
                applicationContext,
                100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        //setting the repeating alarm that will be triggered every day
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun checkLoginStatus() {
        if (sharedPref.getLoginStatus()) {
            startActivity(Intent(this, Categories::class.java))
            finish()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_APP_UPDATE -> {
                    displayToast(getString(R.string.app_update_failed))
                    finishAffinity()
                }
            }
        }
    }
}