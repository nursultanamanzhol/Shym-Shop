package com.shym.commercial.extensions

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import androidx.core.app.ActivityOptionsCompat
import android.content.Intent
import com.shym.commercial.R

object ProgressDialogUtil {

    fun showProgressDialog(context: Context): Dialog {
        val progressDialog = Dialog(context)
        progressDialog.setContentView(R.layout.dialog_progress)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(0))
        progressDialog.show()
        return progressDialog
    }

    fun hideProgressDialog1(progressDialog: Dialog, destination: Class<*>, context: Context) {
        Handler().postDelayed({
            if (!(context as Activity).isFinishing) {
                progressDialog.dismiss()
                val intent = Intent(context, destination)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    context,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                context.startActivity(intent, options.toBundle())
            }
        }, 1000)
    }


    fun hideProgressDialog(progressDialog: Dialog, destination: Class<*>, context: Context) {
        Handler().postDelayed({
            progressDialog.dismiss()
            val intent = Intent(context, destination)
            val options = ActivityOptionsCompat.makeCustomAnimation(
                context,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            context.startActivity(intent, options.toBundle())
        }, 1000)
    }
}
