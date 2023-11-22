package com.shym.commercial.users_role

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.shym.commercial.MainActivity
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityMainUserPageBinding

class MainUserPage : AppCompatActivity() {
    private lateinit var binding: ActivityMainUserPageBinding

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainUserPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //        swipe update
        refreshApp()
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.logRl.setOnClickListener {
            startActivity(Intent(this, BiometricAuthenticationActivity::class.java))
        }
        binding.loginBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    private fun refreshApp() {
        binding.swipeContainer.setOnRefreshListener {
            binding.swipeContainer.isRefreshing = false
        }
    }


    private fun checkUser() {
        // get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //not logged in, user can stay in user dashboard without login toolbar
            binding.subTitleTv.text = "Not logged In"

            binding.profileBtn.visibility = View.GONE
        } else {
            //logged in, get and show user info
            val email = firebaseUser.email
            //set to textview of toolbar
            binding.subTitleTv.text = email
            binding.profileBtn.visibility = View.VISIBLE
        }
    }
}