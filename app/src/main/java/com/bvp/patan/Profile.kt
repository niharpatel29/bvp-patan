package com.bvp.patan

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bvp.patan.adapter.ContentAdapter
import com.bvp.patan.model.ListItemUserDetails
import com.bvp.patan.model.UserModel
import com.bvp.patan.operations.ImageOperations
import com.bvp.patan.operations.Operations
import com.bvp.patan.prefs.SharedPref
import com.bvp.patan.sqlite.MyDBHandler
import kotlinx.android.synthetic.main.profile.*

class Profile : AppCompatActivity() {

    private lateinit var sharedPref: SharedPref
    private lateinit var operations: Operations
    private lateinit var progressDialog: ProgressDialog
    private lateinit var imageOperations: ImageOperations
    private lateinit var dbHandler: MyDBHandler
    private lateinit var contentAdapter: ContentAdapter
    private lateinit var userDetails: UserModel
    private var contentList = ArrayList<ListItemUserDetails>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        sharedPref = SharedPref(this)
        operations = Operations(this)
        progressDialog = ProgressDialog(this)
        imageOperations = ImageOperations(this)
        dbHandler = MyDBHandler(this)


        toolbar()
    }

    override fun onStart() {
        userDetails = sharedPref.getAllDetails()

        setHeaders()
        setContents()
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_edit -> {
                startActivity(Intent(this, ModifyProfile::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon?.setColorFilter(
            resources.getColor(R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
        toolbar.overflowIcon?.setColorFilter(
            resources.getColor(R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    private fun setHeaders() {
        imageOperations.setProfilePicture(imgProfile)
        val name = "${userDetails.firstname} ${userDetails.lastname}"
        tvName.text = name
        tvPosition.text = userDetails.position
    }

    private fun addItem(icon: Int, label: Int, value: String?) {
        if (value!!.isNotEmpty()) {
            contentList.add(ListItemUserDetails(icon, getString(label), value))
        }
    }

    private fun contents() {
        val city = "${userDetails.city}, ${userDetails.state}"

        addItem(R.drawable.ic_baseline_phone, R.string.mobile, userDetails.mobilePrimary)
        addItem(
            R.drawable.ic_baseline_phone,
            R.string.mobile_secondary,
            userDetails.mobileSecondary
        )
        addItem(R.drawable.ic_baseline_email, R.string.email, userDetails.email)
        addItem(R.drawable.ic_baseline_cake, R.string.birth_date, userDetails.dob)
        addItem(R.drawable.ic_baseline_rings, R.string.anniversary, userDetails.anniversary)
        addItem(R.drawable.ic_baseline_drop, R.string.bloodgroup, userDetails.bloodgroup)
        addItem(
            R.drawable.ic_baseline_home,
            R.string.residential_address,
            userDetails.residentialAddress
        )
        addItem(R.drawable.ic_baseline_pin, R.string.city, city)
        addItem(R.drawable.ic_baseline_pin, R.string.zipcode, userDetails.zipcode)
        addItem(R.drawable.ic_baseline_star, R.string.branch_position, userDetails.position)
    }

    private fun setContents() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        contentList.clear()

        contents()
        contentAdapter = ContentAdapter(this, contentList)
        recyclerView.adapter = contentAdapter
    }
}