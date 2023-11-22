package com.shym.commercial.users_role

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hanks.passcodeview.PasscodeView
import com.kevalpatel.passcodeview.PinView
import com.shym.commercial.MainActivity
import com.shym.commercial.R
import com.shym.commercial.users_role.admin.DashboardAdminActivity
import com.shym.commercial.users_role.customer.DashboardUserActivity
import com.shym.commercial.users_role.salesman.DashboardSalesmanPageActivity


const val PASSCODE_LENGTH = 4

class BiometricAuthenticationActivity : AppCompatActivity() {
    companion object {
        private const val ARG_CURRENT_PIN = "current_pin"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var passcodeView2: PasscodeView
    private lateinit var mPinView: PinView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric_authentication)

        firebaseAuth = FirebaseAuth.getInstance()



        passcodeView2 = findViewById(R.id.passcodeView)
        setupPasscode()
    }


    private fun setupPasscode() {
        passcodeView2.setPasscodeLength(PASSCODE_LENGTH)
            .setLocalPasscode("1234")
            .setListener(object : com.hanks.passcodeview.PasscodeView.PasscodeViewListener {
                override fun onFail() {
                    Toast.makeText(applicationContext, "Fail", Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(number: String?) {
                    checkUser()
                }

            })
    }

    private fun checkUser() {
        // get current user, if logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //user not logged in, goto main screen
            startActivity(Intent(this@BiometricAuthenticationActivity, MainActivity::class.java))
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
                        val categoryId = ds.child("id").getValue(String::class.java)
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

                                val userType = userSnapshot.child("userType").value

                                if (userType == "user") {
                                    startActivity(
                                        Intent(
                                            this@BiometricAuthenticationActivity,
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
                                                this@BiometricAuthenticationActivity,
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
                                            this@BiometricAuthenticationActivity,
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
