package com.shym.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.shym.bookapp.users_role.admin.DashboardAdminActivity
import com.shym.bookapp.users_role.customer.DashboardUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.bookapp.users_role.salesman.DashboardSalesmanPageActivity

class SplashActivity : AppCompatActivity() {

//    // view binding
//    private lateinit var binding : ActivitySplashBinding

//    // firebase user
//    private lateinit var firebaseUser: FirebaseUser

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Скрываем навигационную панель и часы
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed(Runnable {
            checkUser()
        }, 500) //means 1 seconds
    }

    private fun checkUser() {
        // get current user, if logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //user not logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            //user logged in, check user type, same as done in login screen

//            progressDialog.setMessage("Checking User...")

            val firebaseUser = firebaseAuth.currentUser!!

            // Firebase
            val categoriesRef = FirebaseDatabase.getInstance().getReference("Categories")
            val usersRef = FirebaseDatabase.getInstance().getReference("Users")

            categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(categoriesSnapshot: DataSnapshot) {
                    val categoryArrayList: ArrayList<String> = ArrayList()

                    for (ds in categoriesSnapshot.children) {
                        val category = ds.child("category").getValue(String::class.java)
                        val categoryId = ds.child("id").getValue(String::class.java)
                        if (categoryId != null) {
                            categoryArrayList.add(categoryId)
                        }
                    }

                    // Теперь у вас есть список всех категорий из "Categories"

                    // Проверим UserType
                    usersRef.child(firebaseUser.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
//                                progressDialog.dismiss()

                                val userType = userSnapshot.child("userType").value

                                if (userType == "user") {
                                    startActivity(
                                        Intent(
                                            this@SplashActivity,
                                            DashboardUserActivity::class.java
                                        )
                                    )
                                    finish()
                                } else if (userType == "salesman") {
                                    // Проверим, есть ли у пользователя категория в списке
                                    val userCategories = userSnapshot.child("categories").value
                                    if (userCategories != null && userCategories is String) {
                                        if (categoryArrayList.contains(userCategories)) {
                                            val intent = Intent(
                                                this@SplashActivity,
                                                DashboardSalesmanPageActivity::class.java
                                            )
                                            intent.putExtra("categoryId", userCategories)
                                            Log.d("TAG_Splash_activity", "onDataChange: ${userCategories}")
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                } else if (userType == "admin") {
                                    startActivity(
                                        Intent(
                                            this@SplashActivity,
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
}
/*Keep user logged in
* 1) Check if user logged in
* 2) check type of user*/