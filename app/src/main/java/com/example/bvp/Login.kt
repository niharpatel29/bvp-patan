package com.example.bvp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.firebase.Topic
import com.example.bvp.model.UserModel
import com.example.bvp.operations.ImageOperations
import com.example.bvp.operations.Operations
import com.example.bvp.prefs.SharedPref
import com.example.bvp.response.AllUsers
import com.example.bvp.response.UserLogin
import com.example.bvp.sqlite.MyDBHandler
import kotlinx.android.synthetic.main.login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class Login : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginTAG"
    }

    private lateinit var sharedPref: SharedPref
    private lateinit var operations: Operations
    private lateinit var dbHandler: MyDBHandler
    private lateinit var imageOperations: ImageOperations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        sharedPref = SharedPref(this)
        operations = Operations(this)
        dbHandler = MyDBHandler(this)
        imageOperations = ImageOperations(this)
        Topic(this).run { subscribe(global) }

        checkLoginStatus()
        handleButtonClicks()
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
            if (operations.checkNullOrEmpty(layoutMobile)) {
                return@setOnClickListener
            }
            if (isPasswordLayoutVisible()) {
                if (operations.checkNullOrEmpty(layoutPassword)) {
                    return@setOnClickListener
                }
            }
            // check_user_exist if not empty
            val userMobile = operations.getValue(layoutMobile)
            val userPassword = operations.getValue(layoutPassword)

            userLogin(userMobile, userPassword)
        }
    }

    private fun isPasswordLayoutVisible(): Boolean {
        return layoutPassword.visibility == View.VISIBLE
    }

    private fun userLogin(userMobile: String, userPassword: String) {
        operations.showProgressDialog()

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = if (!isPasswordLayoutVisible()) {
            apiService.performUserLogin(userMobile)
        } else {
            apiService.performUserLogin(userMobile, userPassword)
        }

        call.enqueue(object : Callback<UserLogin> {
            override fun onFailure(call: Call<UserLogin>, t: Throwable) {
                operations.hideProgressDialog()
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

                            try {
                                sharedPref.run {
                                    setLoginStatus(true)
                                    setId(userId)
                                    setMobilePrimary(mobilePrimary)
                                    setFirstname(firstName)
                                    setMiddlename(middleName)
                                    setLastname(lastName)
                                    setMobileSecondary(mobileSecondary)
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
                                }
                            } catch (e: Exception) {
                                operations.displayToast(e.message.toString())
                            }

                            handleSubscription()
                            getAllUsersFromServer(userMobile, userPassword)
                        }
                        "failed" -> {
                            operations.hideProgressDialog()
                            operations.displayToast(getString(R.string.login_failed))
                        }
                        "able_to_login" -> {
                            operations.hideProgressDialog()
                            actionOnAbleToLogin()
                        }
                        "able_to_signup" -> {
                            operations.hideProgressDialog()
                            startActivity(
                                Intent(this@Login, Signup::class.java)
                                    .putExtra("user_mobile", userMobile)
                            )
                        }
                        "user_not_exist" -> {
                            operations.hideProgressDialog()
                            operations.displayToast(getString(R.string.user_not_exist))
                        }
                        "error" -> {
                            operations.hideProgressDialog()
                            operations.displayToast(getString(R.string.sql_error))
                        }
                        else -> {
                            operations.hideProgressDialog()
                            operations.displayToast(getString(R.string.unknown_error))
                        }
                    }
                } else {
                    operations.hideProgressDialog()
                    operations.displayToast(getString(R.string.response_failed))
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
            subscribe(sharedPref.getPosition()!!.toLowerCase(Locale.getDefault()))
        }
    }

    private fun getAllUsersFromServer(userMobile: String, userPassword: String) {
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
                                dbHandler.addUser(mUser)
                            }
                            startActivity(Intent(this@Login, Categories::class.java))
                            finish()
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
}