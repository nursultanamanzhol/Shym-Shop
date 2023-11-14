package com.shym.bookapp.users_role.salesman

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.bookapp.MainActivity
import com.shym.bookapp.R
import com.shym.bookapp.category.CategoryAddActivity
import com.shym.bookapp.category.UploadPdfActivity
import com.shym.bookapp.category.adapter.AdapterCategory
import com.shym.bookapp.databinding.ActivityDashboardAdminBinding
import com.shym.bookapp.databinding.ActivityDashboardSalesmanBinding
import com.shym.bookapp.databinding.RowCategoryBinding
import com.shym.bookapp.models.ModelCategory

class DashboardSalesmanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardSalesmanBinding

    private lateinit var row_holder: RowCategoryBinding

    //hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //adapter
    private lateinit var adapterCategory: AdapterCategory

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardSalesmanBinding.inflate(layoutInflater)
        row_holder = RowCategoryBinding.inflate(layoutInflater)
        loadCategories()
        uploadPdfFile()
        search()
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        BottomSheetBehavior.from(binding.sheet).apply {
            peekHeight = 100
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }


        setContentView(binding.root)
    }


    private fun uploadPdfFile() {
        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, UploadPdfActivity::class.java))
        }

        //handle click, logout
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
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


            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.emailTv.text = snapshot.child("name").value.toString()
                        // get user type e.g. user or admin
//                        val userType = snapshot.child("userType").value

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
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
                adapterCategory =
                    AdapterCategory(this@DashboardSalesmanActivity, categoryArrayList)
//                //set adapter Rv
                binding.categoriesRv.adapter = adapterCategory


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun search() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

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
}
