package com.shym.bookapp.users_role.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.shym.bookapp.R
import com.shym.bookapp.databinding.ActivityProfileBinding

private lateinit var binding: ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)


        setContentView(binding.root)
    }
}