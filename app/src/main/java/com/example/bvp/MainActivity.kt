package com.example.bvp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bvp.admin.AdminLogin
import com.example.bvp.operations.Operations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleButtonClicks()
    }

    private fun handleButtonClicks() {
        btnAdminLogin.setOnClickListener {
            startActivity(Intent(this, AdminLogin::class.java))
        }

        btnUserLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }
}