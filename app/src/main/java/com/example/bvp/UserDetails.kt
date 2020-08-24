package com.example.bvp

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bvp.adapter.ContentAdapter
import com.example.bvp.model.UserListItem
import com.example.bvp.model.UserModel
import com.example.bvp.operations.Operations
import com.example.bvp.sqlite.MyDBHandler
import kotlinx.android.synthetic.main.user_details.*

class UserDetails : AppCompatActivity() {

    companion object {
        const val TAG = "UserDetailsTAG"
    }

    private lateinit var dbHandler: MyDBHandler
    private lateinit var operations: Operations
    private lateinit var contentAdapter: ContentAdapter

    private var contentList = ArrayList<UserListItem>()
    private lateinit var userId: String
    private lateinit var userDetails: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_details)

        dbHandler = MyDBHandler(this)
        operations = Operations(this)
        userId = intent.getStringExtra("user_id")!!
        userDetails = dbHandler.getUserDetails(userId)

        toolbar()
        handleFabClicks()
        setHeaders()
        setContents()
    }

    private fun toolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setColorFilter(
            resources.getColor(R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    private fun handleFabClicks() {
        fabCall.setOnClickListener {
            performAction(R.string.type_call)
        }

        fabSMS.setOnClickListener {
            performAction(R.string.type_sms)
        }

        fabMail.setOnClickListener {
            operations.sendEmail(userDetails.email)
        }

        fabWhatsapp.setOnClickListener {
            performAction(R.string.type_whatsapp)
        }
    }

    private fun performAction(actionType: Int) {
        operations.mobileChooser(
            getString(actionType),
            userDetails.mobilePrimary,
            userDetails.mobileSecondary
        )
    }

    private fun setHeaders() {
        val name = "${userDetails.firstname} ${userDetails.lastname}"
        val mobilePrimary = userDetails.mobilePrimary
        val position = userDetails.position

        tvName.text = name
        tvMobilePrimary.text = mobilePrimary
        tvPosition.text = position
    }

    private fun addItem(icon: Int, label: Int, value: String?) {
        if (value!!.isNotEmpty()) {
            contentList.add(UserListItem(icon, getString(label), value))
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
        addItem(R.drawable.ic_baseline_cake, R.string.anniversary, userDetails.anniversary)
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