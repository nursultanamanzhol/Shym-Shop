package com.shym.commercial

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat

object DialogUtils {

    fun createProgressDialog(context: Context, message: String): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_progress)
        dialog.findViewById<TextView>(R.id.textViewMessage).text = message
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        return dialog
    }


}
