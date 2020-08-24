package com.example.bvp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.example.bvp.api.postClient
import com.example.bvp.operations.ImageOperations
import com.example.bvp.operations.Operations
import com.example.bvp.prefs.SharedPref
import com.example.bvp.sqlite.MyDBHandler
import kotlinx.android.synthetic.main.categories.*
import java.io.File

class Categories : AppCompatActivity() {

    companion object {
        const val TAG = "CategoryTAG"
    }

    private lateinit var sharedPref: SharedPref
    private lateinit var dbHandler: MyDBHandler
    private lateinit var operations: Operations
    private lateinit var imageOperations: ImageOperations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.categories)

        sharedPref = SharedPref(this)
        dbHandler = MyDBHandler(this)
        operations = Operations(this)
        imageOperations = ImageOperations(this)

        val root = Environment.getExternalStorageDirectory()
        val dir = File(root.absolutePath.toString() + "/bp/")
        Log.d(TAG, dir.toString())
        if (!dir.exists()) {
            dir.mkdir()
            Log.d(TAG, "not exist")
        }

        toolbar()
        handleButtonClicks()
        downloadProfilePicture()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, Profile::class.java))
                true
            }
            R.id.action_logout -> {
                logout()
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
        btnMembers.setOnClickListener {
            startActivity(Intent(this, UserDirectory::class.java))
        }

        btnNewsletters.setOnClickListener {
            startActivity(Intent(this, Newsletters::class.java))
        }
    }

    private fun downloadProfilePicture() {
        val mFile = File(imageOperations.profilePicturePath, imageOperations.fileName)
        if (mFile.exists()) {
            operations.displayToast("exist")
            return
        }

        val baseUrl = postClient()!!.baseUrl().toString()
        val file = "FileUploader/Uploads/ProfilePictures/${sharedPref.getId()}.jpg"
        val url = "$baseUrl$file"
        Log.d(TAG, url)

        Glide
            .with(this)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.d(TAG, "error loading file")
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    imageOperations.saveToInternalStorage(resource)
                }
            })
    }

    private fun logout() {
        sharedPref.userLogout()
        dbHandler.clearDatabase()
        deleteProfilePicture()

        startActivity(Intent(this, Login::class.java))
        finish()
    }

    private fun deleteProfilePicture() {
        val file = File(imageOperations.profilePicturePath, imageOperations.fileName)
        if (file.exists()) {
            file.delete()
        }
    }
}