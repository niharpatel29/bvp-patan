package com.example.bvp.admin

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bvp.R


class UploadNewsletter : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_newsletter)

        handleButtonClicks()
    }

    private fun handleButtonClicks() {
        showFileChooser()
    }

    private val FILE_SELECT_CODE = 0

    private fun showFileChooser() {
        Intent(Intent.ACTION_GET_CONTENT).also {
            it.type = "*/*"
            it.addCategory(Intent.CATEGORY_OPENABLE)
            try {
                startActivityForResult(
                    Intent.createChooser(it, "Select a File to Upload"),
                    FILE_SELECT_CODE
                )
            } catch (ex: ActivityNotFoundException) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(
                    this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}