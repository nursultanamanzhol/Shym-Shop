package com.shym.commercial

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
            // Ключевой момент: если это вызывается внутри активити, используйте 'finish()',
            // если внутри фрагмента или другого контекста, нужно использовать другой метод для завершения текущей активности или фрагмента.
            // Например, для фрагмента используйте fragmentManager.popBackStack().
            // finish()
        }, 1000)
    }
}
