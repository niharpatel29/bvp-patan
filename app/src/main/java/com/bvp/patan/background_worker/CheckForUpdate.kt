package com.bvp.patan.background_worker

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.bvp.patan.Login
import com.bvp.patan.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability

class CheckForUpdate(val context: Context) : AsyncTask<Void, Void, Boolean>() {

    companion object {
        const val TAG = "CheckForUpdateTAG"
    }

    override fun onPreExecute() {
        super.onPreExecute()
        Log.d(TAG, "update check started")
    }

    override fun doInBackground(vararg p0: Void?): Boolean {
        val appUpdateManager = AppUpdateManagerFactory.create(context)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        var updateAvailable = true
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                updateAvailable = true
                Log.d(Login.TAG, "xxxUpdate available")
            } else {
                updateAvailable = false
                Log.d(Login.TAG, "xxxNo new updates")
            }
        }
        return updateAvailable
    }

    override fun onPostExecute(updateAvailable: Boolean) {
        super.onPostExecute(updateAvailable)
        if (updateAvailable) {
//            showUpdateDialog()
            Log.d(TAG, "Update available")
        } else {
            Log.d(TAG, "No new updates")
        }
    }

    private fun showUpdateDialog() {
//        val dialog = AlertDialog.Builder(context)
        val dialog = MaterialAlertDialogBuilder(context)

        dialog
            .setTitle("Update available")
            .setMessage("Please update app to continue")
            .setCancelable(true)

        dialog.setPositiveButton(context.getString(R.string.update)) { Dialog, id ->
            redirectToPlayStore()
            Dialog.dismiss()
        }

        dialog.setNegativeButton(context.getString(R.string.cancel)) { Dialog, id ->
            Dialog.cancel()
        }

        dialog.setOnCancelListener {
            (context as Activity).finishAffinity()
        }

        try {
            dialog.create().show()
        } catch (e: IllegalStateException) {
            Log.e(TAG, e.message.toString())
        }
    }

    private fun redirectToPlayStore() {
        val appPackageName = context.packageName

        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: ActivityNotFoundException) {
            Log.e(Login.TAG, e.message.toString())
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
        /*try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                this,
                CheckForUpdate.REQUEST_CODE_APP_UPDATE
            )
        } catch (e: IntentSender.SendIntentException) {
            skipIfLoggedIn()
            Log.d(TAG, e.message.toString())
        }*/
    }
}