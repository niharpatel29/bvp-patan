package com.bvp.patan.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.bvp.patan.R
import com.bvp.patan.operations.Operations
import kotlinx.android.synthetic.main.chooser.*

class Chooser(
    context: Context,
    private val actionType: String,
    private val mobilePrimary: String?,
    private val mobileSecondary: String?
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.chooser)

        tvMobilePrimary.text = mobilePrimary
        tvMobileSecondary.text = mobileSecondary

        tvMobilePrimary.setOnClickListener {
            performAction(mobilePrimary)
            dismiss()
        }
        tvMobileSecondary.setOnClickListener {
            performAction(mobileSecondary)
            dismiss()
        }
    }

    private fun performAction(mobile: String?) {
        if (actionType == context.getString(R.string.type_call)) {
            Operations(context).performCall(mobile)
        }
        if (actionType == context.getString(R.string.type_sms)) {
            Operations(context).sendSMS(mobile)
        }
        if (actionType == context.getString(R.string.type_whatsapp)) {
            Operations(context).sendToWhatsapp(mobile)
        }
    }
}