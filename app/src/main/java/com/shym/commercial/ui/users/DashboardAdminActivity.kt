package com.shym.commercial.ui.users

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.shym.commercial.ui.main.MainActivity
import com.shym.commercial.adapters.AdapterCategory
import com.shym.commercial.ui.category.CategoryAddActivity
import com.shym.commercial.data.model.ModelCategory
import com.shym.commercial.ui.category.UploadPdfActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityDashboardAdminBinding
import com.shym.commercial.databinding.HeaderDrawerBinding
import com.shym.commercial.databinding.RowCategoryBinding
import com.shym.commercial.ui.main.MainUserPage
import com.shym.commercial.ui.profile.ProfileActivity

class DashboardAdminActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityDashboardAdminBinding
    private lateinit var bindHeader: HeaderDrawerBinding
    private lateinit var row_holder: RowCategoryBinding

    //hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //adapter
    private lateinit var adapterCategory: AdapterCategory

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private var backPressedTime = 0L
    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {
            Toast.makeText(applicationContext, "Press back again to exit app", Toast.LENGTH_SHORT)
                .show()
        }
        startActivity(Intent(this@DashboardAdminActivity, MainUserPage::class.java))

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        row_holder = RowCategoryBinding.inflate(layoutInflater)
        bindHeader = HeaderDrawerBinding.inflate(layoutInflater)
        // Здесь используйте свой макет
        setContentView(binding.root)

        uploadPdfFile()
//        closeWindow()
        loadCategories()

        //init firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
//        firebaseAuth = FirebaseAuth.getInstance()
//        loadUserInfo()

        binding.apply {
            navHeader.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.profile -> {
                        startActivity(
                            Intent(
                                this@DashboardAdminActivity,
                                ProfileActivity::class.java
                            )
                        )
                    }

                    R.id.add_category_menu -> {
                        startActivity(
                            Intent(
                                this@DashboardAdminActivity,
                                CategoryAddActivity::class.java
                            )
                        )
                    }

                    R.id.add_product_menu -> {
                        startActivity(
                            Intent(
                                this@DashboardAdminActivity,
                                UploadPdfActivity::class.java
                            )
                        )
                    }
                }
//                drawer.closeDrawer(GravityCompat.START)
                true
            }
            btnNavView.setOnClickListener {
                drawer.openDrawer(GravityCompat.START)
            }
        }


    }

    private fun uploadPdfFile() {

        //handle click, logout
        binding.logoutBtn.setOnClickListener {
            startActivity(Intent(this, MainUserPage::class.java))
            finish()
        }

        //поиск
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterCategory.filter.filter(s)


                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }


    private fun loadCategories() {
        //init arraylist
        categoryArrayList = ArrayList()
        //get all categories from firebase database...
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clean list before adding info it
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    //get data as model
                    val model = ds.getValue(ModelCategory::class.java)

                    //add to arr
                    categoryArrayList.add(model!!)
                }
                //setup adapter
                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList)
//                //set adapter Rv
                binding.categoriesRv.adapter = adapterCategory

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun checkUser() {
        // get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //not logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            //set to textview of toolbar
            binding.subTitleTv.text = firebaseUser.email
            val navigationView: NavigationView = findViewById(R.id.nav_header)
            val menu: Menu = navigationView.menu
            val emailItem: MenuItem = menu.findItem(R.id.email)
            val currentUser = firebaseAuth.currentUser
            currentUser?.let {
                val userEmail = currentUser.email
                emailItem.title = userEmail
            }
        }
    }

}

