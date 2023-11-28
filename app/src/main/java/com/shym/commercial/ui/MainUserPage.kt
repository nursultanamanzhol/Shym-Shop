package com.shym.commercial.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityMainUserPageBinding
import com.shym.commercial.extensions.setSafeOnClickListener
import com.shym.commercial.ui.admin.DashboardAdminActivity
import com.shym.commercial.ui.profile.ProfileActivity
import com.shym.commercial.ui.customer.DashboardUserActivity
import com.shym.commercial.ui.salesman.DashboardSalesmanPageActivity

class MainUserPage : AppCompatActivity() {
    private lateinit var binding: ActivityMainUserPageBinding

    private lateinit var switchMode: SwitchCompat
    private var nightMode: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainUserPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //switchMode
        switchLDMode()

        //swipe update
        refreshApp()
        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        checkingUser()
        navPages()

    }

    private fun refreshApp() {
        binding.swipeContainer.setSafeOnClickListener {
            binding.swipeContainer.isRefreshing = false
        }
    }

    private fun navPages() {

        binding.logRl.setSafeOnClickListener {
//            checkUser()
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.control.setSafeOnClickListener {
            checkUser()
//            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.logOutBtn.setSafeOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.allProduct.setSafeOnClickListener {
            startActivity(Intent(this, DashboardUserActivity::class.java))
            finish()
        }
    }


    private fun checkingUser() {
        val firebaseUser =
            if (firebaseAuth.currentUser != null) firebaseAuth.currentUser else throw NullPointerException(
                "Expression 'firebaseAuth.currentUser' must not be null"
            )
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        if (firebaseUser != null) {
            usersRef.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(userSnapshot: DataSnapshot) {
                        //                                progressDialog.dismiss()
                        val firebaseUser = firebaseAuth.currentUser
                        val userType = userSnapshot.child("userType").value
                        if (userType == "user") {
                            binding.control.visibility = View.GONE
                            // get current user
                        } else if (userType == "admin" || userType == "salesman") {
                            binding.control.visibility = View.VISIBLE
                        }
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

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }
    }

    private fun checkUser() {
        // get current user, if logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //user not logged in, goto main screen
            startActivity(Intent(this@MainUserPage, MainActivity::class.java))
            finish()
        } else {
            //user logged in, check user type, same as done in login screen

            //            progressDialog.setMessage("Checking User...")

            val firebaseUser = firebaseAuth.currentUser!!

            // Firebase
            val categoriesRef =
                FirebaseDatabase.getInstance().getReference("Categories")
            val usersRef = FirebaseDatabase.getInstance().getReference("Users")

            categoriesRef.addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(categoriesSnapshot: DataSnapshot) {
                    val categoryArrayList: ArrayList<String> = ArrayList()

                    for (ds in categoriesSnapshot.children) {
                        val category =
                            ds.child("category").getValue(String::class.java)
                        val categoryId =
                            ds.child("id").getValue(String::class.java)
                        if (categoryId != null) {
                            categoryArrayList.add(categoryId)
                        }
                    }

                    // Теперь у вас есть список всех категорий из "Categories"

                    // Проверим UserType
                    usersRef.child(firebaseUser.uid)
                        .addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                //                                progressDialog.dismiss()

                                val userType =
                                    userSnapshot.child("userType").value

                                if (userType == "user") {
                                    startActivity(
                                        Intent(
                                            this@MainUserPage,
                                            DashboardUserActivity::class.java
                                        )
                                    )
                                    finish()
                                } else if (userType == "salesman") {
                                    // Проверим, есть ли у пользователя категория в списке
                                    val userCategories =
                                        userSnapshot.child("categories").value
                                    if (userCategories != null && userCategories is String) {
                                        if (categoryArrayList.contains(
                                                userCategories
                                            )
                                        ) {
                                            val intent = Intent(
                                                this@MainUserPage,
                                                DashboardSalesmanPageActivity::class.java
                                            )
                                            intent.putExtra(
                                                "categoryId",
                                                userCategories
                                            )
                                            Log.d(
                                                "TAG_PassCode_activity",
                                                "onDataChange: ${userCategories}"
                                            )
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                } else if (userType == "admin") {
                                    startActivity(
                                        Intent(
                                            this@MainUserPage,
                                            DashboardAdminActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Обработка ошибок
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    // Обработка ошибок
                }
            })

        }
    }

    private fun switchLDMode() {
        //switchMode
        switchMode = binding.switchMode
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE)
        nightMode = sharedPreferences.getBoolean("nightMode", false)
        if (nightMode) {
            switchMode.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        switchMode.setSafeOnClickListener {
            if (nightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor = sharedPreferences.edit()
                editor.putBoolean("nightMode", false)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor = sharedPreferences.edit()
                editor.putBoolean("nightMode", true)
            }
            editor.apply()
        }
    }

    private fun loadUserInfo() {
        //db firebase user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    //set date
//                    binding.nameTv.text = name
                    //set image
                    try {
                        Glide.with(this@MainUserPage)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileBtn)
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}
