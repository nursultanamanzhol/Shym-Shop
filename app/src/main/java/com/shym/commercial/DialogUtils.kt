package com.shym.commercial

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat

object DialogUtils {

    // Создание диалога с анимированным переходом
    fun showAnimatedDialog(context: Context, destination: Class<*>, message: String) {
        val dialog = createProgressDialog(context, message)

        // Отложенное закрытие диалога и запуск новой активности
        Handler().postDelayed({
            dialog.dismiss()
            val intent = Intent(context, destination)
            val options = ActivityOptionsCompat.makeCustomAnimation(
                context,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            context.startActivity(intent, options.toBundle())
        }, 1000)

        dialog.show()
    }

    // Создание диалога прогресса
    fun createProgressDialog(context: Context, message: String): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_progress)
        dialog.findViewById<TextView>(R.id.textViewMessage).text = message
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        return dialog
    }
}

