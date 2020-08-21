package com.example.bvp.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bvp.R
import kotlinx.android.synthetic.main.admin_actions.*

class AdminActions : AppCompatActivity() {

    private lateinit var sharedPrefAdmin: SharedPrefAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_actions)

        sharedPrefAdmin = SharedPrefAdmin(this)

        handleButtonClicks()
    }

    override fun onBackPressed() {
        logoutDialog()
    }

    private fun handleButtonClicks() {
        btnRegisterNewUser.setOnClickListener {
            startActivity(Intent(this, AdminRegistersNewUser::class.java))
        }

        btnUploadNewsletter.setOnClickListener {
            startActivity(Intent(this, UploadNewsletter::class.java))
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
                sharedPrefAdmin.adminLogout()
                startActivity(Intent(this, AdminLogin::class.java))
                finish()
            }

        builder
            .setNegativeButton(getString(R.string.cancel)) { Dialog, id ->
                Dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }
}