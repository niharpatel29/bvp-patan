package com.bvp.patan.admin

import android.app.DatePickerDialog
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.bvp.patan.R
import com.bvp.patan.api.APIInterface
import com.bvp.patan.api.postClient
import com.bvp.patan.model.Country
import com.bvp.patan.model.UserModel
import com.bvp.patan.operations.Operations
import com.bvp.patan.response.GeneralResponse
import com.bvp.patan.sqlite.MyDBHandler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.modify_profile.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdminModifyUserProfile : AppCompatActivity() {

    companion object {
        const val TAG = "AdminModifyUserProfileTAG"
        const val defaultValue = ""
    }

    private lateinit var operations: Operations
    private lateinit var dbHandler: MyDBHandler
    private lateinit var countryList: ArrayList<Country>
    private lateinit var userId: String
    private lateinit var user: UserModel

    private var selectedDOB: String? = null
    private var selectedAnniversary: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modify_profile)

        operations = Operations(this)
        dbHandler = MyDBHandler(this)

        userId = intent.getStringExtra("user_id")!!
        user = dbHandler.getUserDetails(userId)

        toolbar()
        listener()
        initialCalls()
        setValuesOnStart()
        handleButtonClicks()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        goBackDialog()
    }

    private fun toolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tvTitle.visibility = View.GONE
        supportActionBar?.title = getString(R.string.modify_user_profile)
        toolbar.navigationIcon?.setColorFilter(
            resources.getColor(R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    private fun initialCalls() {
        linearLayout.visibility = View.GONE
        linearLayoutPosition.visibility = View.VISIBLE
        exposedDropdown(getString(R.string.type_bloodgroup))
        exposedDropdown(getString(R.string.type_gender))
        exposedDropdown(getString(R.string.type_country))
    }

    private fun goBackDialog() {
        val dialog = MaterialAlertDialogBuilder(this)

        dialog
            .setTitle(getString(R.string.title_go_back))
            .setMessage(getString(R.string.message_go_back))
            .setCancelable(true)

        dialog.setPositiveButton(getString(R.string.ok)) { Dialog, id ->
            Dialog.dismiss()
            super.onBackPressed()
        }

        dialog.setNegativeButton(getString(R.string.cancel)) { Dialog, id ->
            Dialog.dismiss()
        }

        dialog.create().show()
    }

    private fun setValuesOnStart() {
        operations.run {
            val name = "${user.firstname} ${user.lastname}"
            tvName.text = name

            setText(layoutFirstname, user.firstname)
            setText(layoutMiddlename, user.middlename)
            setText(layoutLastname, user.lastname)
            setText(layoutMobileSecondary, user.mobileSecondary)
            setText(layoutEmail, user.email)
            setText(layoutDOB, user.dob)
            setText(layoutAnniversary, user.anniversary)
            setText(layoutBloodgroup, user.bloodgroup)
            setText(layoutGender, user.gender)
            setText(layoutCountry, user.country)
            setText(layoutState, user.state)
            setText(layoutCity, user.city)
            setText(layoutZipcode, user.zipcode)
            setText(layoutResidentialAddress, user.residentialAddress)
            setText(layoutPosition, user.position)
            if (user.category == getString(R.string.topic_general)) {
                radioGeneral.isChecked = true
                radioKarobari.isChecked = false

                layoutPosition.visibility = View.GONE
                layoutPosition.editText!!.text = null
            } else {
                radioGeneral.isChecked = false
                radioKarobari.isChecked = true
            }
        }
    }

    private fun listener() {
        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (radioGeneral.isChecked) {
                layoutPosition.visibility = View.GONE
                layoutPosition.editText!!.text = null
            } else {
                layoutPosition.visibility = View.VISIBLE
            }
        }
    }

    private fun handleButtonClicks() {
        layoutDOB.editText!!.setOnClickListener {
            selectDOB()
        }

        layoutAnniversary.editText!!.setOnClickListener {
            selectAnniversary()
        }

        btnUpdate.setOnClickListener {
            // VALIDATION IS DONE ONLY ON MANDATORY ELEMENTS.
            if (operations.checkNullOrEmpty(layoutFirstname)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutLastname)) {
                return@setOnClickListener
            }
            if (radioGroup.checkedRadioButtonId <= 0) {
                setRadioError(true)
                return@setOnClickListener
            } else {
                setRadioError(false)
            }

            val userId = user.userId
            val mobilePrimary = user.mobilePrimary

            val firstName = operations.getValue(layoutFirstname)
            val middleName = operations.getValue(layoutMiddlename)
            val lastName = operations.getValue(layoutLastname)
            val mobileSecondary = operations.getValue(layoutMobileSecondary)
            val email = operations.getValue(layoutEmail)
            val dob = operations.getValue(layoutDOB)
            val anniversary = operations.getValue(layoutAnniversary)
            val bloodgroup = operations.getValue(layoutBloodgroup)
            val gender = operations.getValue(layoutGender)
            val country = operations.getValue(layoutCountry)
            val state = operations.getValue(layoutState)
            val city = operations.getValue(layoutCity)
            val zipcode = operations.getValue(layoutZipcode)
            val residentialAddress = operations.getValue(layoutResidentialAddress)
            val category =
                if (radioGeneral.isChecked) getString(R.string.topic_general) else getString(R.string.topic_karobari)
            val position = operations.getValue(layoutPosition)

            val userDetails = UserModel(
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
            updateUserDetailsToServer(userDetails)
        }
    }

    private fun setRadioError(error: Boolean) {
        if (error) {
            radioKarobari.error = getString(R.string.required_field)
            radioGeneral.error = getString(R.string.required_field)
        } else {
            radioKarobari.error = null
            radioGeneral.error = null
        }
    }

    private fun selectDOB() {
        val calendar = Calendar.getInstance()
        val date = user.dob

        if (date != defaultValue && date != "") {
            if (selectedDOB == null) {
                selectedDOB = date
            }
            calendar.time = stringToDate(selectedDOB)
        } else {
            if (selectedDOB != null) {
                calendar.time = stringToDate(selectedDOB)
            }
        }

        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, { view, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDOB = dateToString(calendar)

                layoutDOB.editText?.setText(selectedDOB)
            },
            mYear,
            mMonth,
            mDay
        )
        datePickerDialog.datePicker.maxDate = Date().time
        datePickerDialog.setCancelable(false)
        datePickerDialog.show()
    }

    private fun selectAnniversary() {
        val calendar = Calendar.getInstance()
        val date = user.anniversary

        if (date != defaultValue && date != "") {
            if (selectedAnniversary == null) {
                selectedAnniversary = date
            }
            calendar.time = stringToDate(selectedAnniversary)
        } else {
            if (selectedAnniversary != null) {
                calendar.time = stringToDate(selectedAnniversary)
            }
        }

        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, { view, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedAnniversary = dateToString(calendar)

                layoutAnniversary.editText?.setText(selectedAnniversary)
            },
            mYear,
            mMonth,
            mDay
        )
        datePickerDialog.datePicker.maxDate = Date().time
        datePickerDialog.setCancelable(false)
        datePickerDialog.show()
    }

    private fun stringToDate(date: String?): Date {
        val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        return simpleDateFormat.parse(date!!)!!
    }

    private fun dateToString(calendar: Calendar): String {
        val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }

    private fun exposedDropdown(type: String) {
        if (type == getString(R.string.type_bloodgroup)) {
            val items = resources.getStringArray(R.array.blood_type)
            val adapter = ArrayAdapter(this, R.layout.exposed_dropdown_list_item, items)
            val textView = layoutBloodgroup.editText as AutoCompleteTextView
            textView.setAdapter(adapter)
        }

        if (type == getString(R.string.type_gender)) {
            val items = resources.getStringArray(R.array.gender)
            val adapter = ArrayAdapter(this, R.layout.exposed_dropdown_list_item, items)
            val textView = layoutGender.editText as AutoCompleteTextView
            textView.setAdapter(adapter)
        }

        if (type == getString(R.string.type_country)) {
            val items = getCountries()
            val adapter = ArrayAdapter(this, R.layout.exposed_dropdown_list_item, items)
            val textView = layoutCountry.editText as AutoCompleteTextView
            textView.setAdapter(adapter)
        }
    }

    private fun getCountries(): ArrayList<Country> {
        countryList = ArrayList()

        val json = applicationContext.assets.open("countries.json").bufferedReader().use {
            it.readText()
        }

        val jsonObject = JSONObject(json)
        val jsonArray = jsonObject.getJSONArray("countries")

        val myArray: ArrayList<Country> = Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<List<Country>>() {}.type
        )
        countryList.clear()
        countryList.addAll(myArray)

        return countryList
    }

    private fun updateUserDetailsToServer(userModel: UserModel) {
        operations.showProgressDialog()

        val userId = userModel.userId
        val mobilePrimary = user.mobilePrimary
        val firstName = userModel.firstname
        val middleName = userModel.middlename
        val lastName = userModel.lastname
        val mobileSecondary = userModel.mobileSecondary
        val email = userModel.email
        val dob = userModel.dob
        val anniversary = userModel.anniversary
        val bloodgroup = userModel.bloodgroup
        val gender = userModel.gender
        val country = userModel.country
        val state = userModel.state
        val city = userModel.city
        val zipcode = userModel.zipcode
        val residentialAddress = userModel.residentialAddress
        val position = user.position
        val category = user.category

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performUpdateUserProfile(
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

        call.enqueue(object : Callback<GeneralResponse> {
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                operations.hideProgressDialog()
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                if (response.isSuccessful) {
                    when (response.body()!!.response) {
                        "ok" -> {
                            operations.displayToast(getString(R.string.updated_successfully))
                            operations.hideProgressDialog()
                            finish()
                        }
                        "failed" -> {
                            operations.displayToast(getString(R.string.update_failed))
                            operations.hideProgressDialog()
                        }
                        "error" -> {
                            //will set alert dialog instead of this
                            operations.displayToast(getString(R.string.sql_error))
                            operations.hideProgressDialog()
                        }
                        else -> {
                            operations.displayToast(getString(R.string.unknown_error))
                            Log.d(TAG, response.body()!!.message)
                            operations.hideProgressDialog()
                        }
                    }
                } else {
                    operations.displayToast(getString(R.string.response_failed))
                    operations.hideProgressDialog()
                }
            }
        })
    }
}