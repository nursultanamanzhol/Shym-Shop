package com.example.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.bookapp.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashActivity : AppCompatActivity() {

//    // view binding
//    private lateinit var binding : ActivitySplashBinding

//    // firebase user
//    private lateinit var firebaseUser: FirebaseUser

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed(Runnable{
            checkUser()
        }, 1000) //means 1 seconds
    }

    private fun checkUser() {
        // get current user, if logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            //user not logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
/*Keep user logged in
* 1) Check if user logged in
* 2) check type of user*/