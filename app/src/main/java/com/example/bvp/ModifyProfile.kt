package com.example.bvp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.bvp.api.APIInterface
import com.example.bvp.api.postClient
import com.example.bvp.model.Country
import com.example.bvp.model.UserModel
import com.example.bvp.operations.ImageOperations
import com.example.bvp.operations.Operations
import com.example.bvp.prefs.SharedPref
import com.example.bvp.response.GeneralResponse
import com.example.bvp.response.UploadResponse
import com.example.bvp.upload.UploadRequestBody
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yalantis.ucrop.UCrop
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.modify_profile.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ModifyProfile : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 101
        const val TAG = "ModifyActivityTAG"
    }

    private lateinit var operations: Operations
    private lateinit var sharedPref: SharedPref
    private lateinit var imageOperations: ImageOperations
    private lateinit var countryList: ArrayList<Country>

    private var selectedDOB: String? = null
    private var selectedAnniversary: String? = null
    private var selectedImageFlag = false
    private lateinit var fileFromCache: Bitmap
    private lateinit var tempCacheFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modify_profile)

        operations = Operations(this)
        sharedPref = SharedPref(this)
        imageOperations = ImageOperations(this)

        toolbar()
        initOperations()
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
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon?.setColorFilter(
            resources.getColor(R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    private fun initOperations() {
        exposedDropdown(getString(R.string.type_bloodgroup))
        exposedDropdown(getString(R.string.type_gender))
        exposedDropdown(getString(R.string.type_country))
    }

    private fun goBackDialog() {
        val builder = AlertDialog.Builder(this)

        builder
            .setTitle(getString(R.string.title_go_back))
            .setMessage(getString(R.string.message_go_back))
            .setCancelable(true)

        builder
            .setPositiveButton(getString(R.string.ok)) { Dialog, id ->
                Dialog.dismiss()
                super.onBackPressed()
            }

        builder
            .setNegativeButton(getString(R.string.cancel)) { Dialog, id ->
                Dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun setHeaders() {
        imageOperations.setProfilePicture(imgProfile)
        val name = "${sharedPref.getFirstname()} ${sharedPref.getLastname()}"
        tvName.text = name
        tvPosition.text = sharedPref.getPosition()
    }

    private fun setValuesOnStart() {
        setHeaders()
        operations.run {
            val name = "${sharedPref.getFirstname()} ${sharedPref.getLastname()}"
            tvName.text = name

            setText(layoutFirstname, sharedPref.getFirstname())
            setText(layoutMiddlename, sharedPref.getMiddlename())
            setText(layoutLastname, sharedPref.getLastname())
            setText(layoutMobileSecondary, sharedPref.getMobileSecondary())
            setText(layoutEmail, sharedPref.getEmail())
            setText(layoutDOB, sharedPref.getDOB())
            setText(layoutAnniversary, sharedPref.getAnniversary())
            setText(layoutBloodgroup, sharedPref.getBloodGroup())
            setText(layoutGender, sharedPref.getGender())
            setText(layoutCountry, sharedPref.getCountry())
            setText(layoutState, sharedPref.getState())
            setText(layoutCity, sharedPref.getCity())
            setText(layoutZipcode, sharedPref.getZipcode())
            setText(layoutResidentialAddress, sharedPref.getResidentialAddress())
        }
    }

    private fun handleButtonClicks() {
        btnChangeProfile.setOnClickListener {
            openImageChooser()
        }

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
            if (operations.checkNullOrEmpty(layoutDOB)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutBloodgroup)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutGender)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutZipcode)) {
                return@setOnClickListener
            }
            if (operations.checkNullOrEmpty(layoutResidentialAddress)) {
                return@setOnClickListener
            }

            val userId = sharedPref.getId()
            val mobilePrimary = sharedPref.getMobilePrimary()

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
                null
            )
            updateUserDetailsToServer(userDetails)
        }
    }

    private fun selectDOB() {
        val calendar = Calendar.getInstance()
        val date = sharedPref.getDOB()

        if (date != sharedPref.defaultValue() && date != "") {
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
            this,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
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
        val date = sharedPref.getAnniversary()

        if (date != sharedPref.defaultValue() && date != "") {
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
            this,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
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

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.performUpdateUserProfile(
            userId,
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
            residentialAddress
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
                            sharedPref.run {
                                setFirstname(firstName)
                                setMiddlename(middleName)
                                setLastname(lastName)
                                setMobileSecondary(mobileSecondary)
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

                            uploadImage()
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

    private fun uploadImage() {
        if (!selectedImageFlag) {
            operations.displayToast(getString(R.string.updated_successfully))
            operations.hideProgressDialog()
            finish()
            return
        }

        operations.showProgressDialog()
        val body = UploadRequestBody(tempCacheFile, "image")

        val apiService = postClient()!!.create(APIInterface::class.java)
        val call = apiService.uploadProfilePicture(
            MultipartBody.Part.createFormData(
                "file",
                tempCacheFile.name,
                body
            ),
            sharedPref.getId()!!.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        )

        call.enqueue(object : Callback<UploadResponse> {
            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                Log.d("onFailure", t.toString())
                operations.hideProgressDialog()
            }

            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                if (response.isSuccessful) {
                    val mResponse = response.body()
                    when (mResponse!!.error) {
                        false -> {
                            selectedImageFlag = false
                            // save file permanently
                            if (imageOperations.saveToInternalStorage(fileFromCache)) {
                                imageOperations.setProfilePicture(imgProfile)
                            }
                            Log.d(TAG, mResponse.message)
                            operations.displayToast(getString(R.string.updated_successfully))
                            finish()
                        }
                        true -> {
                            operations.displayToast(mResponse.message)
                        }
                    }
                    operations.hideProgressDialog()
                } else {
                    operations.displayToast(getString(R.string.response_failed))
                }
            }
        })
    }

    private fun openImageChooser() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .also {
                val mimeTypes = arrayOf("image/jpg", "image/png", "image/jpeg")
                it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                startActivityForResult(it, REQUEST_CODE_PICK_IMAGE)
            }
    }

    private fun cropImage(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, contentResolver.getFileName(sourceUri)))

        val options = UCrop.Options()
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.colorWhite))
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.colorPrimary))
        options.withAspectRatio(1F, 1F)

        UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .start(this)
    }

    private fun ContentResolver.getFileName(uri: Uri): String {
        var fileName = ""
        val cursor = this.query(uri, null, null, null, null)
        if (cursor != null) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            fileName = cursor.getString(nameIndex)
            cursor.close()
        }
        return fileName
    }

    // automatically converts any FORMAT to .jpg
    private fun compressImage(imageFile: File) {
        lifecycleScope.launch {
            val compressedImage = Compressor.compress(this@ModifyProfile, imageFile)
            compressedImage.let {
                fileFromCache = BitmapFactory.decodeFile(it.absolutePath)
                tempCacheFile = File(it.absolutePath)
                imgProfile.setImageBitmap(fileFromCache)
                Log.d(TAG, "Compressed default image path: ${it.absolutePath}")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PICK_IMAGE -> {
                    cropImage(data!!.data!!)
                }
                UCrop.REQUEST_CROP -> {
                    val resultUri = UCrop.getOutput(data!!)
                    val imageFile = File(resultUri!!.path!!)
                    compressImage(imageFile)
                    selectedImageFlag = true
                }
                UCrop.RESULT_ERROR -> {
                    val cropError = UCrop.getError(data!!)
                    Log.e(TAG, "Crop error: $cropError")
                }
            }
        }
    }
}