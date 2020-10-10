package com.bvp.patan.operations

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.bvp.patan.R
import com.bvp.patan.dialog.Chooser
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

//class Operations(private val context: Context) {

val bvpDirectory = "/BVP Patan/"

fun getValue(value: TextInputLayout): String {
    return value.editText?.text?.trim().toString()
}

fun getValueET(value: EditText): String {
    return value.text.trim().toString()
}

fun Context.passwordMatch(pass: TextInputLayout, confirmPass: TextInputLayout): Boolean {
    //return TRUE if match
    return if (getValue(pass) != getValue(confirmPass)) {
        confirmPass.error = this.getString(R.string.password_not_match)
        false
    } else {
        confirmPass.error = null
        true
    }
}

fun Context.checkNullOrEmpty(textInputLayout: TextInputLayout): Boolean {
    val value = getValue(textInputLayout)

    return if (value.isEmpty()) {
        textInputLayout.error = this.getString(R.string.required_field)
        true
    } else {
        textInputLayout.error = null
        false
    }
}

fun Context.checkNullOrEmptyET(editText: EditText): Boolean {
    val value = getValueET(editText)

    return if (value.isEmpty()) {
        editText.error = this.getString(R.string.required_field)
        true
    } else {
        editText.error = null
        false
    }
}

fun setText(textInputLayout: TextInputLayout, value: String?) {
    textInputLayout.editText!!.setText(value)
}

fun Context.performCall(mobile: String?) {
    val callIntent = Intent(Intent.ACTION_DIAL)
    callIntent.data = Uri.parse("tel:$mobile")
    this.startActivity(callIntent)
}

fun Context.sendSMS(mobile: String?) {
    val uri = Uri.parse("smsto:$mobile")
    val smsIntent = Intent(Intent.ACTION_SENDTO, uri)

    try {
        this.startActivity(smsIntent)
    } catch (ex: ActivityNotFoundException) {
        displayToast("Can't resolve app for ACTION_SENDTO Intent")
    }
}

fun Context.sendEmail(emailId: String?) {
    val emailIntent = Intent(Intent.ACTION_SENDTO)
    emailIntent.data = Uri.parse("mailto:$emailId")

    try {
        this.startActivity(Intent.createChooser(emailIntent, "Send mail using"))
    } catch (ex: ActivityNotFoundException) {
        displayToast("There are no email clients installed")
    }
}

fun Context.sendToWhatsapp(mobile: String?) {
    val uri = Uri.parse("smsto:$mobile")
    val whatsappIntent = Intent(Intent.ACTION_SENDTO, uri)
    whatsappIntent.setPackage("com.whatsapp")

    try {
        this.startActivity(whatsappIntent)
    } catch (ex: ActivityNotFoundException) {
        displayToast("Whatsapp not installed")
    }
}

fun Context.mobileChooser(
    actionType: String,
    mobilePrimary: String?,
    mobileSecondary: String?
) {
    if (mobileSecondary!!.isNotEmpty()) {
        val dialog = Chooser(this, actionType, mobilePrimary, mobileSecondary)
        dialog.setCancelable(true)
        dialog.show()

        val window = dialog.window!!
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawableResource(android.R.color.transparent)
    } else {
        if (actionType == this.getString(R.string.type_call)) {
            performCall(mobilePrimary)
        }
        if (actionType == this.getString(R.string.type_sms)) {
            sendSMS(mobilePrimary)
        }
        if (actionType == this.getString(R.string.type_whatsapp)) {
            sendToWhatsapp(mobilePrimary)
        }
    }
}

fun Context.internetAvailable(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val activeNetworkInfo = connectivityManager!!.activeNetworkInfo

    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

private lateinit var progressDialog: ProgressDialog

fun Context.showProgressDialog() {
    progressDialog = ProgressDialog(this)

    progressDialog.setMessage(this.getString(R.string.please_wait))
    progressDialog.setCancelable(false)
    progressDialog.show()
}

fun hideProgressDialog() {
    progressDialog.dismiss()
}

fun Context.displayToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun View.displaySnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
}
//}