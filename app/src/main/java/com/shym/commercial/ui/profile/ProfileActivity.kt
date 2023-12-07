package com.shym.commercial.ui.profile

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityProfileBinding
import com.shym.commercial.extensions.setSafeOnClickListener
import com.shym.commercial.ui.main.MainUserPage
import com.shym.commercial.viewmodel.ProfileViewModel


class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileViewModel

    private lateinit var firebaseAuth: FirebaseAuth


    private var backPressedTime = 0L
    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {

        }
        startActivity(Intent(this@ProfileActivity, MainUserPage::class.java))
//        backPressedTime = System.currentTimeMillis()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //animation
        animation()

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        observeUserInfo()

        //handle click go back
        binding.backBtn.setSafeOnClickListener {
            startActivity(Intent(this, MainUserPage::class.java))
            finish()
        }
        binding.profileEditBtn.setSafeOnClickListener() {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }
        binding.cameraImg.setSafeOnClickListener() {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }
    }
    private fun observeUserInfo() {
        viewModel.userInfo.observe(this) { userInfo ->
            binding.nameTv.text = userInfo.name
            binding.emailTv.text = userInfo.email
            binding.accountTypeTv.text = userInfo.userType
            binding.memberDateTv.text = userInfo.memberDate

            try {
                Glide.with(this@ProfileActivity)
                    .load(userInfo.profileImage)
                    .placeholder(R.drawable.ic_person_gray)
                    .into(binding.profileImg)
            } catch (e: Exception) {
                // Обработка ошибок, если необходимо
            }
        }
    }



//    private fun loadUserInfo() {
//        //db firebase user info
//        val ref = FirebaseDatabase.getInstance().getReference("Users")
//        ref.child(firebaseAuth.uid!!)
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    //get user info
//                    val email = "${snapshot.child("email").value}"
//                    val name = "${snapshot.child("name").value}"
//                    val profileImage = "${snapshot.child("profileImage").value}"
//                    val timestamp = "${snapshot.child("timestamp").value}"
//                    val uid = "${snapshot.child("uid").value}"
//                    val userType = "${snapshot.child("userType").value}"
//                    val quickAccessCode = "${snapshot.child("quickAccessCode").value}"
//
//                    //convert timestamp to proper date format
//                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())
//
//                    //set date
//                    binding.nameTv.text = name
//                    binding.emailTv.text = email
//                    binding.accountTypeTv.text = userType
//                    binding.memberDateTv.text = formattedDate
//                    //set image
//                    try {
//                        Glide.with(this@ProfileActivity)
//                            .load(profileImage)
//                            .placeholder(R.drawable.ic_person_gray)
//                            .into(binding.profileImg)
//                    } catch (e: Exception) {
//
//                    }
//
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//            })
//    }
    private fun animation() {
        //initialized animation
        var fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        var bottom_down = AnimationUtils.loadAnimation(this, R.anim.bottom_down)
        //settings the bottom down
        binding.profileMainConst.animation = bottom_down
        binding.accountLn.animation = bottom_down
        binding.memberLn.animation = bottom_down
        binding.favoriteLn.animation = bottom_down

        //handler
        var handler = Handler()
        var runnable = Runnable() {
            //lets set fadeIN animation on other layouts
        }

        handler.postDelayed(runnable, 1000)

    }
}