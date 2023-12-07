package com.shym.commercial.ui.users

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.commercial.ui.main.MainActivity
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityDashboardSalesmanPageBinding
import com.shym.commercial.data.model.ModelPdf
import com.shym.commercial.ui.main.MainUserPage
import com.shym.commercial.ui.profile.ProfileActivity
import com.shym.commercial.adapters.AdapterPdfSalesman
import com.shym.commercial.databinding.HeaderDrawerBinding
import com.shym.commercial.ui.category.UploadPdfActivitySalesman

class DashboardSalesmanPageActivity : AppCompatActivity() {
    //viewBinding
    private lateinit var binding: ActivityDashboardSalesmanPageBinding
    private lateinit var bindHeader: HeaderDrawerBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private companion object {
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    private var categoryId = ""
    private var category = ""

    //arrayList to hold books
    private lateinit var pdfArrayList: ArrayList<ModelPdf>

    //adapter
    private lateinit var adapterPdfSalesman: AdapterPdfSalesman
    private var backPressedTime = 0L
    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {
            Toast.makeText(applicationContext, "Press back again to exit app", Toast.LENGTH_SHORT)
                .show()
        }
        startActivity(Intent(this@DashboardSalesmanPageActivity, MainUserPage::class.java))
//        backPressedTime = System.currentTimeMillis()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardSalesmanPageBinding.inflate(layoutInflater)
        bindHeader = HeaderDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //init firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

// теперь, когда firebaseAuth инициализирован, вызов checkUser
        checkUser()

        logout()
//        val intent = intent
//        categoryId = intent.getStringExtra("categoryId") ?: intent.getStringExtra("categoryUp")!!
        loadPdfList()

        binding.apply {
            navHeaderSalesman.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.profile -> {
                        startActivity(
                            Intent(
                                this@DashboardSalesmanPageActivity,
                                ProfileActivity::class.java
                            )
                        )
                    }

                    R.id.add_product_menu_salesman -> {
                        val intent = Intent(
                            this@DashboardSalesmanPageActivity,
                            UploadPdfActivitySalesman::class.java
                        )
//                        intent.putExtra("categoryId", categoryId)
                        startActivity(intent)

                    }
                }
//                drawer.closeDrawer(GravityCompat.START)
                true
            }
            btnNavView.setOnClickListener {
                drawer.openDrawer(GravityCompat.START)
            }
        }


        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //filter data
                try {
                    adapterPdfSalesman.filter.filter(s)
                } catch (e: Exception) {
                    Log.d(TAG, "onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        //handle click, go back
//        binding.backBtn.setOnClickListener {
//            onBackPressed()
//        }

    }

    fun logout() {
        //handle click, logout
        binding.logoutBtn.setOnClickListener {
            startActivity(Intent(this, MainUserPage::class.java))
            finish()
        }
    }

    private fun loadPdfList() {
        //init arraylist
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        (if (firebaseAuth.uid != null) firebaseAuth.uid else throw NullPointerException("Expression 'firebaseAuth.uid' must not be null"))?.let {
            userRef.child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get user info
                        val name = "${snapshot.child("name").value}"
                        val categories = "${snapshot.child("categories").value}"
                        val profileImage = "${snapshot.child("profileImage").value}"

                        ref.orderByChild("categoryId").equalTo(categories)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    //clear list before start adding data into it
                                    pdfArrayList.clear()
                                    for (ds in snapshot.children) {
                                        //get data
                                        val model = ds.getValue(ModelPdf::class.java)
                                        //add to list
                                        if (model != null) {
                                            pdfArrayList.add(model)
                                            Log.d(
                                                TAG,
                                                "onDataChange: ${model.title} ${model.categoryId}"
                                            )
                                        }
                                    }

                                    //setup adapter
                                    adapterPdfSalesman = AdapterPdfSalesman(
                                        this@DashboardSalesmanPageActivity,
                                        pdfArrayList
                                    )
                                    binding.booksRsv.adapter = adapterPdfSalesman
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })


                        //set date
                        bindHeader?.nameTv?.text = name
                        //set image
                        try {
                            if (!profileImage.isNullOrEmpty()) {
                                Glide.with(this@DashboardSalesmanPageActivity)
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_person_gray)
                                    .into(bindHeader?.profileImg!!)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }


    }


    private fun checkUser() {
        // get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //not logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            //logged in, get and show user info
            //set to textview of toolbar
            binding.subTitleTv.text = firebaseUser.email
            val navigationView: NavigationView = findViewById(R.id.navHeaderSalesman)
            val menu: Menu = navigationView.menu
            val emailItem: MenuItem = menu.findItem(R.id.emailSalesman)
            val currentUser = firebaseAuth.currentUser
            currentUser?.let {
                val userEmail = currentUser.email
                emailItem.title = userEmail
            }
        }
    }
}