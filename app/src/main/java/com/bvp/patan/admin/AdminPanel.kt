package com.bvp.patan.admin

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bvp.patan.R
import com.bvp.patan.admin.other.SharedPrefAdmin
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_change_password -> {
                startActivity(Intent(this, ChangeAdminPassword::class.java))
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

        btnDeleteUser.setOnClickListener {
            startActivity(
                Intent(this, UserList::class.java)
                    .putExtra("type", getString(R.string.type_delete_user))
            )
        }

        btnModifyUserProfile.setOnClickListener {
            startActivity(
                Intent(this, UserList::class.java)
                    .putExtra("type", getString(R.string.type_modify_user))
            )
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

        btnMakeNewAdmin.setOnClickListener {
            startActivity(
                Intent(this, UserList::class.java)
                    .putExtra("type", getString(R.string.type_make_admin))
            )
        }
    }

    private fun logoutDialog() {
        val dialog = MaterialAlertDialogBuilder(this)

        dialog
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.confirm_logout))
            .setCancelable(true)

        dialog
            .setPositiveButton(getString(R.string.logout)) { Dialog, id ->
                Dialog.dismiss()
                logout()
            }

        dialog
            .setNegativeButton(getString(R.string.cancel)) { Dialog, id ->
                Dialog.dismiss()
            }

        dialog.create().show()
    }

    private fun logout() {
        sharedPrefAdmin.adminLogout()
        startActivity(Intent(this, AdminLogin::class.java))
        finish()
    }
}