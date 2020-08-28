package com.example.bvp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.announcement.*

class Announcement : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.announcement)

        handleButtonClicks()
    }

    private fun handleButtonClicks() {
        btnMarkRead.setOnClickListener {
            finish()
        }
    }
}