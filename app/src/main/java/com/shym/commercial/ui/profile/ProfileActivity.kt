package com.shym.commercial.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityProfileBinding
import com.shym.commercial.extensions.setSafeOnClickListener
import com.shym.commercial.pdflist.MyApplication


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        //handle click go back
        binding.backBtn.setSafeOnClickListener {
            onBackPressed()
        }
        binding.profileEditBtn.setSafeOnClickListener() {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }


    }

    private fun loadUserInfo() {
        //db firebase user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"
                    val quickAccessCode = "${snapshot.child("quickAccessCode").value}"

                    //convert timestamp to proper date format
                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    //set date
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.accountTypeTv.text = userType
                    binding.memberDateTv.text = formattedDate
                    //set image
                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileImg)
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}