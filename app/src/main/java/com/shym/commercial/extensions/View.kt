package com.shym.commercial.extensions
// Extensions.kt

import android.view.View
import com.shym.commercial.extensions.SafeClickListener

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}
