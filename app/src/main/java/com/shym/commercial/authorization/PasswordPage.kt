package com.shym.commercial.authorization

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.commercial.DialogUtils
import com.shym.commercial.MainActivity
import com.shym.commercial.ProgressDialogUtil
import com.shym.commercial.R
import com.shym.commercial.extensions.setSafeOnClickListener
import com.shym.commercial.users_role.MainUserPage
import com.shym.commercial.databinding.ActivityPasswordPageBinding

class PasswordPage : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityPasswordPageBinding
    private var editPassword = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logOutBtn.setSafeOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        firebaseAuth = FirebaseAuth.getInstance()

        val btn1: LinearLayout = binding.btn1
        val btn2: LinearLayout = binding.btn2
        val btn3: LinearLayout = binding.btn3
        val btn4: LinearLayout = binding.btn4
        val btn5: LinearLayout = binding.btn5
        val btn6: LinearLayout = binding.btn6
        val btn7: LinearLayout = binding.btn7
        val btn8: LinearLayout = binding.btn8
        val btn9: LinearLayout = binding.btn9
        val btn0: LinearLayout = binding.btn0
        val checkBtn: LinearLayout = binding.btnCheck
        val deleteBtn: LinearLayout = binding.deleteBtn
        val passwordText: TextView = binding.passwordTex

        btn1.setSafeOnClickListener { appendDigit(1) }
        btn2.setSafeOnClickListener { appendDigit(2) }
        btn3.setSafeOnClickListener { appendDigit(3) }
        btn4.setSafeOnClickListener { appendDigit(4) }
        btn5.setSafeOnClickListener { appendDigit(5) }
        btn6.setSafeOnClickListener { appendDigit(6) }
        btn7.setSafeOnClickListener { appendDigit(7) }
        btn8.setSafeOnClickListener { appendDigit(8) }
        btn9.setSafeOnClickListener { appendDigit(9) }
        btn0.setSafeOnClickListener { appendDigit(0) }

        val firebaseUser = firebaseAuth.currentUser!!
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        usersRef.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    password = userSnapshot.child("quickAccessCode").value.toString()
                    checkBtn.setSafeOnClickListener {
                        if (editPassword.length == 4) {
                            if (editPassword == password) {
                                goOnPages()
                                Toast.makeText(
                                    this@PasswordPage,
                                    "Правильно...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                passwordText.text = null
                                editPassword = ""
                                Toast.makeText(
                                    this@PasswordPage,
                                    "Пароль неверный",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@PasswordPage,
                                "Пароль должен состоять из 4 цифр",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    deleteBtn.setSafeOnClickListener {
                        if (editPassword.isNotBlank()) {
                            editPassword =
                                editPassword.subSequence(0, editPassword.length - 1).toString()
                            passwordText.text = editPassword
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

    private fun goOnPages() {
        val progressDialog = DialogUtils.createProgressDialog(this, "Login...")
        progressDialog.show()
        startActivity(Intent(this, MainUserPage::class.java))
    }

    private fun appendDigit(digit: Int) {
        if (editPassword.length < 4) {
            editPassword += digit
            binding.passwordTex.text = editPassword
        }
    }
}

//    private fun checkUser() {
//        // get current user, if logged in or not
//        val firebaseUser = firebaseAuth.currentUser
//        if (firebaseUser == null) {
//            //user not logged in, goto main screen
//            startActivity(Intent(this@PasswordPage, MainActivity::class.java))
//            finish()
//        } else {
//            //user logged in, check user type, same as done in login screen
//
////            progressDialog.setMessage("Checking User...")
//
//            val firebaseUser = firebaseAuth.currentUser!!
//
//            // Firebase
//            val categoriesRef =
//                FirebaseDatabase.getInstance().getReference("Categories")
//            val usersRef = FirebaseDatabase.getInstance().getReference("Users")
//
//            categoriesRef.addListenerForSingleValueEvent(object :
//                ValueEventListener {
//                override fun onDataChange(categoriesSnapshot: DataSnapshot) {
//                    val categoryArrayList: ArrayList<String> = ArrayList()
//
//                    for (ds in categoriesSnapshot.children) {
//                        val category =
//                            ds.child("category").getValue(String::class.java)
//                        val categoryId = ds.child("id").getValue(String::class.java)
//                        if (categoryId != null) {
//                            categoryArrayList.add(categoryId)
//                        }
//                    }
//
//                    // Теперь у вас есть список всех категорий из "Categories"
//
//                    // Проверим UserType
//                    usersRef.child(firebaseUser.uid)
//                        .addListenerForSingleValueEvent(object :
//                            ValueEventListener {
//                            override fun onDataChange(userSnapshot: DataSnapshot) {
////                                progressDialog.dismiss()
//
//                                val userType = userSnapshot.child("userType").value
//
//                                if (userType == "user") {
//                                    startActivity(
//                                        Intent(
//                                            this@PasswordPage,
//                                            DashboardUserActivity::class.java
//                                        )
//                                    )
//                                    finish()
//                                } else if (userType == "salesman") {
//                                    // Проверим, есть ли у пользователя категория в списке
//                                    val userCategories =
//                                        userSnapshot.child("categories").value
//                                    if (userCategories != null && userCategories is String) {
//                                        if (categoryArrayList.contains(
//                                                userCategories
//                                            )
//                                        ) {
//                                            val intent = Intent(
//                                                this@PasswordPage,
//                                                DashboardSalesmanPageActivity::class.java
//                                            )
//                                            intent.putExtra(
//                                                "categoryId",
//                                                userCategories
//                                            )
//                                            Log.d(
//                                                "TAG_PassCode_activity",
//                                                "onDataChange: ${userCategories}"
//                                            )
//                                            startActivity(intent)
//                                            finish()
//                                        }
//                                    }
//                                } else if (userType == "admin") {
//                                    startActivity(
//                                        Intent(
//                                            this@PasswordPage,
//                                            DashboardAdminActivity::class.java
//                                        )
//                                    )
//                                    finish()
//                                }
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//                                // Обработка ошибок
//                            }
//                        })
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    // Обработка ошибок
//                }
//            })
//
//        }
//    }

