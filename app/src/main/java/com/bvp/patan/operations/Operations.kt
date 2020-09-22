package com.bvp.patan.operations

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.bvp.patan.R
import com.bvp.patan.dialog.Chooser
import com.google.android.material.textfield.TextInputLayout

class Operations(private val context: Context) {

    val bvpDirectory = "/BVP Patan/"
    private val progressDialog = ProgressDialog(context)

    fun getValue(value: TextInputLayout): String {
        return value.editText?.text?.trim().toString()
    }

    fun getValueET(value: EditText): String {
        return value.text.trim().toString()
    }

    fun passwordMatch(pass: TextInputLayout, confirmPass: TextInputLayout): Boolean {
        //return TRUE if match
        return if (getValue(pass) != getValue(confirmPass)) {
            confirmPass.error = context.getString(R.string.password_not_match)
            false
        } else {
            confirmPass.error = null
            true
        }
    }

    fun checkNullOrEmpty(textInputLayout: TextInputLayout): Boolean {
        val value = getValue(textInputLayout)

        return if (value.isEmpty()) {
            textInputLayout.error = context.getString(R.string.required_field)
            true
        } else {
            textInputLayout.error = null
            false
        }
    }

    fun checkNullOrEmptyET(editText: EditText): Boolean {
        val value = getValueET(editText)

        return if (value.isEmpty()) {
            editText.error = context.getString(R.string.required_field)
            true
        } else {
            editText.error = null
            false
        }
    }

    fun setText(textInputLayout: TextInputLayout, value: String?) {
        textInputLayout.editText!!.setText(value)
    }

    fun performCall(mobile: String?) {
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:$mobile")
        context.startActivity(callIntent)
    }

    fun sendSMS(mobile: String?) {
        val uri = Uri.parse("smsto:$mobile")
        val smsIntent = Intent(Intent.ACTION_SENDTO, uri)

        try {
            context.startActivity(smsIntent)
        } catch (ex: ActivityNotFoundException) {
            displayToast("Can't resolve app for ACTION_SENDTO Intent")
        }
    }

    fun sendEmail(emailId: String?) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:$emailId")

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send mail using"))
        } catch (ex: ActivityNotFoundException) {
            displayToast("There are no email clients installed")
        }
    }

    fun sendToWhatsapp(mobile: String?) {
        val uri = Uri.parse("smsto:$mobile")
        val whatsappIntent = Intent(Intent.ACTION_SENDTO, uri)
        whatsappIntent.setPackage("com.whatsapp")

        try {
            context.startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            displayToast("Whatsapp not installed")
        }
    }

    fun mobileChooser(
        actionType: String,
        mobilePrimary: String?,
        mobileSecondary: String?
    ) {
        if (mobileSecondary!!.isNotEmpty()) {
            val dialog = Chooser(context, actionType, mobilePrimary, mobileSecondary)
            dialog.setCancelable(true)
            dialog.show()

            val window = dialog.window!!
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window.setBackgroundDrawableResource(android.R.color.transparent)
        } else {
            if (actionType == context.getString(R.string.type_call)) {
                performCall(mobilePrimary)
            }
            if (actionType == context.getString(R.string.type_sms)) {
                sendSMS(mobilePrimary)
            }
            if (actionType == context.getString(R.string.type_whatsapp)) {
                sendToWhatsapp(mobilePrimary)
            }
        }
    }

    fun showProgressDialog() {
        progressDialog.setMessage(context.getString(R.string.please_wait))
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    fun hideProgressDialog() {
        progressDialog.dismiss()
    }

    fun displayToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}