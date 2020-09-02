package com.example.bvp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.bvp.admin.AdminLogin
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d("fcmTAG", "destroyed")
    }
}