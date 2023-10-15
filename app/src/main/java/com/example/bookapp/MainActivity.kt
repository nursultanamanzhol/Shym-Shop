package com.example.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.bookapp.authorization.LoginActivity
import com.example.bookapp.dashboard.DashboardUserActivity
import com.example.bookapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding:ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Скрываем навигационную панель и часы
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        //handle click, login
        binding.loginBtn.setOnClickListener{
            //will do later
            startActivity(Intent(this, LoginActivity::class.java))
        }

        //hand click, skip and continue to main screen
        binding.skipBtn.setOnClickListener {
            //will do later
            startActivity(Intent(this, DashboardUserActivity::class.java))
        }

        //now lets connect with firebase

    }
}