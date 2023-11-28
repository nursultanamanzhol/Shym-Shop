package com.shym.commercial

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.shym.commercial.authorization.PasswordPage
import com.shym.commercial.users_role.MainUserPage

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Скрываем навигационную панель и часы
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        FirebaseAuth.getInstance().let { auth ->
            Handler().postDelayed({
                when {
                    auth.currentUser == null -> startActivity(Intent(this, MainActivity::class.java))
                    else -> startActivity(Intent(this, PasswordPage::class.java))
                }
                finish()
            }, 500)
        }
    }
}
