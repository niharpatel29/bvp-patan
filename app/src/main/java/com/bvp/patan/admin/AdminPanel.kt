package com.bvp.patan.admin

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bvp.patan.R
import com.bvp.patan.admin.other.SharedPrefAdmin
import kotlinx.android.synthetic.main.admin_panel.*

class AdminPanel : AppCompatActivity() {

    private lateinit var sharedPrefAdmin: SharedPrefAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_panel)

        sharedPrefAdmin = SharedPrefAdmin(this)

        toolbar()
        handleButtonClicks()
    }

    override fun onBackPressed() {
        logoutDialog()
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

    private fun toolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
        btnRegisterNewUser.setOnClickListener {
            startActivity(Intent(this, AdminRegistersNewUser::class.java))
        }

        btnUploadNewsletter.setOnClickListener {
            startActivity(Intent(this, UploadNewsletter::class.java))
        }

        btnMakeAnnouncement.setOnClickListener {
            startActivity(Intent(this, MakeAnnouncement::class.java))
        }

        btnUploadPhotosLink.setOnClickListener {
            startActivity(Intent(this, UploadPhotosLink::class.java))
        }
    }

    private fun logoutDialog() {
        val builder = AlertDialog.Builder(this)

        builder
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.confirm_logout))
            .setCancelable(true)

        builder
            .setPositiveButton(getString(R.string.logout)) { Dialog, id ->
                Dialog.dismiss()
                logout()
            }

        builder
            .setNegativeButton(getString(R.string.cancel)) { Dialog, id ->
                Dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun logout() {
        sharedPrefAdmin.adminLogout()
        startActivity(Intent(this, AdminLogin::class.java))
        finish()
    }
}