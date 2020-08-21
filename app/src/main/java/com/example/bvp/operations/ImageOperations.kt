package com.example.bvp.operations

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.bvp.R
import com.github.abdularis.civ.CircleImageView
import java.io.*

class ImageOperations(val context: Context) {

    companion object{
        const val TAG = "ImageOperationTAG"
    }

    val profilePicturePath: File = ContextWrapper(context).getDir("profile", Context.MODE_PRIVATE)
    val fileName = "profile_picture.jpg"

    fun saveToInternalStorage(bitmap: Bitmap): Boolean {
        // path to /data/data/yourApp/app_data/profile
        val file = File(profilePicturePath, fileName)

        Log.d(TAG, "file path: $file")
        var fileOutputStream: FileOutputStream? = null
        return try {
            fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            Log.d(TAG, "saved: ${profilePicturePath.absolutePath}")
            true
        } catch (e: IOException) {
            Log.d(TAG, e.message.toString())
            false
        } finally {
            fileOutputStream?.flush()
            fileOutputStream?.close()
        }
    }

    fun setProfilePicture(imageView: CircleImageView) {
        imageView.setImageBitmap(loadFromInternalStorage())
    }

    private fun loadFromInternalStorage(): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val file = File(profilePicturePath.toString(), fileName)
            bitmap = BitmapFactory.decodeStream(FileInputStream(file))
        } catch (e: FileNotFoundException) {
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.default_image)
            Log.d(TAG, e.message.toString())
        } finally {
            return bitmap
        }
    }
}