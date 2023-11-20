package com.shym.bookapp.authorization

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.bookapp.databinding.ActivityLoginBinding
import com.shym.bookapp.users_role.admin.DashboardAdminActivity
import com.shym.bookapp.users_role.customer.DashboardUserActivity
import com.shym.bookapp.users_role.salesman.DashboardSalesmanPageActivity

class LoginActivity : AppCompatActivity() {

    //viewbinding
    private lateinit var binding: ActivityLoginBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerPage()
        // Скрываем навигационную панель и часы
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog, will show while creating account | Register user
        progressDialog = ProgressDialog(this)
//        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, begin login
        binding.loginBtn.setOnClickListener {
            /*Steps
            * 1) Input data
            * 2) Validate data
            * 3) Login - Firebase Auth
            * 4) Check user type - Firebase Auth
            *    If User - Move to user dashboard
            *    If Admin - Move to admin dashboard*/
            validateData()

        }
    }

    fun registerPage() {
        binding.noAccountTv.setOnClickListener {
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Загрузка...")

            // Показываем ProgressDialog
            progressDialog.show()

            // Создаем задачу для Handler, чтобы закрыть ProgressDialog через 2 секунды
            Handler().postDelayed({
                progressDialog.dismiss()

                // После закрытия ProgressDialog запускаем новую активность
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }, 2000) // 2000 миллисекунд (2 секунды)
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        //1) Input data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()


        //2) Validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format...", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show()
        } else {
            loginUser()
        }
    }

    private fun loginUser() {
        //3) Login - Firebase Auth

        //show progress
        progressDialog.setMessage("Logging in...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //login success
                checkUser()

            }
            .addOnFailureListener { e ->
                //failed login
                Toast.makeText(this, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    // В методе checkUser в LoginActivity
    private fun checkUser() {
        progressDialog.setMessage("Checking User...")

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
                            progressDialog.dismiss()

                            val userType = userSnapshot.child("userType").value

                            if (userType == "user") {
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
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
                                            this@LoginActivity,
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
                                        this@LoginActivity,
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