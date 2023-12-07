package com.shym.commercial.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.commercial.R
import com.shym.commercial.adapters.AdapterMainPage
import com.shym.commercial.adapters.AdapterMainSpecial
import com.shym.commercial.adapters.AdapterPdfUser
import com.shym.commercial.data.model.ModelPdf
import com.shym.commercial.databinding.ActivityMainUserPageBinding
import com.shym.commercial.extensions.setSafeOnClickListener
import com.shym.commercial.ui.users.DashboardAdminActivity
import com.shym.commercial.ui.users.DashboardUserActivity
import com.shym.commercial.ui.profile.ProfileActivity
import com.shym.commercial.ui.users.BooksUserFragment
import com.shym.commercial.ui.users.DashboardSalesmanPageActivity
import com.shym.commercial.viewmodel.UserViewModel

class MainUserPage : AppCompatActivity() {
    private lateinit var binding: ActivityMainUserPageBinding
    private val viewModel: UserViewModel by viewModels()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var adapterMainPage: AdapterMainPage
    private lateinit var adapterMainSpecial: AdapterMainSpecial

    private companion object {
        const val TAG = "PDF_MAIN_USER_PAGE_TAG"
    }
    private var categoryId = ""
    private var category = ""
    private var uid = ""
    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private var backPressedTime = 0L

    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finishAffinity() // Завершаем все активности приложения
        } else {
            Toast.makeText(applicationContext, "Press back again to exit app", Toast.LENGTH_SHORT)
                .show()
            backPressedTime = System.currentTimeMillis()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainUserPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadAllBooks()
        loadDiscountBooks()
        //animation
        animation()

        viewModel.userType.observe(this, Observer { userType ->
            if (userType == "user") {
                binding.control.visibility = View.GONE
            } else if (userType == "admin" || userType == "salesman") {
                binding.control.visibility = View.VISIBLE
            }
        })

        viewModel.userEmail.observe(this, Observer { email ->
            if (email == null) {
                binding.subTitleTv.text = "Not logged In"
                binding.profileBtn.visibility = View.GONE
            } else {
                binding.subTitleTv.text = email
                binding.profileBtn.visibility = View.VISIBLE
            }
        })

        viewModel.userCategories.observe(this, Observer { userCategories ->
            // Обработка категорий пользователя, если это необходимо
        })

        binding.logRl.setSafeOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.control.setSafeOnClickListener {
            // Обработка нажатия на контроль, используя ViewModel
            checkUser()
        }

        binding.logOutBtn.setSafeOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.spechialConst.setSafeOnClickListener {
            startActivity(Intent(this, DashboardUserActivity::class.java))
            finish()
        }
        binding.allProduct.setSafeOnClickListener {
            startActivity(Intent(this, DashboardUserActivity::class.java))
            finish()
        }
    }

    private fun loadAllBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books").limitToFirst(10)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPdf::class.java)
                    model?.let { pdfArrayList.add(it) }
                }
                adapterMainPage = AdapterMainPage(this@MainUserPage, pdfArrayList)
                binding.productList1.adapter = adapterMainPage
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "loadAllBooks onCancelled: ${error.message}")
            }
        })
    }

    private fun loadDiscountBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books").limitToFirst(10)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPdf::class.java)
                    if (model != null && model.discount.toInt() > 5) {
                        pdfArrayList.add(model)
                    }
                }
                adapterMainSpecial = AdapterMainSpecial(this@MainUserPage, pdfArrayList)
                binding.productList2.adapter = adapterMainSpecial
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "loadAllBooks onCancelled: ${error.message}")
            }
        })
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

                    // Проверим UserType
                    usersRef.child(firebaseUser.uid)
                        .addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                //                                progressDialog.dismiss()

                                val userType =
                                    userSnapshot.child("userType").value

                                if (userType == "user") {
//                                    startActivity(
//                                        Intent(
//                                            this@MainUserPage,
//                                            DashboardUserActivity::class.java
//                                        )
//                                    )
//                                    finish()
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
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        }
    }

    private fun animation() {
        //initialized animation
        var fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        var bottom_down = AnimationUtils.loadAnimation(this, R.anim.bottom_down)
        //settings the bottom down
        binding.constLogo.animation = bottom_down

        //handler
        var handler = Handler()
        var runnable = Runnable() {
            //lets set fadeIN animation on other layouts
            binding.logOutBtn.animation = fade_in
            binding.loginRegBtnProfile.animation = fade_in
            binding.prid.animation = fade_in
            binding.contactConst.animation = fade_in
            binding.allProduct.animation = fade_in
            binding.spechialConst.animation = fade_in
            binding.control.animation = fade_in
            binding.loginBtn.animation = fade_in

        }

        handler.postDelayed(runnable, 1000)

    }
}

