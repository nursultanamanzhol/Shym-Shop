package com.shym.bookapp.users_role.admin

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.google.android.material.search.SearchView.Behavior
import com.shym.bookapp.MainActivity
import com.shym.bookapp.category.adapter.AdapterCategory
import com.shym.bookapp.category.CategoryAddActivity
import com.shym.bookapp.models.ModelCategory
import com.shym.bookapp.category.UploadPdfActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.bookapp.R
import com.shym.bookapp.databinding.ActivityDashboardAdminBinding
import com.shym.bookapp.databinding.RowCategoryBinding

class DashboardAdminActivity : AppCompatActivity() {
    private lateinit var row_holder: RowCategoryBinding

    //view binding
    private lateinit var binding: ActivityDashboardAdminBinding

    //hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //adapter
    private lateinit var adapterCategory: AdapterCategory

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

//    private lateinit var categoryArrayList: ArrayList<firebaseAuth>

    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private var isDrawerOpen = false // Изначально предполагаем, что панель закрыта

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        row_holder = RowCategoryBinding.inflate(layoutInflater) // Здесь используйте свой макет
        setContentView(binding.root)

        uploadPdfFile()
//        closeWindow()
        loadCategories()

        //init firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

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
                    R.id.add_category_menu ->{
                        startActivity(
                            Intent(
                                this@DashboardAdminActivity,
                                CategoryAddActivity::class.java
                            )
                        )
                    }R.id.add_product_menu ->{
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

//        BottomSheetBehavior.from(binding.sheet).apply {
//            peekHeight = 100
//            this.state = BottomSheetBehavior.STATE_COLLAPSED
//        }

    }

    private fun uploadPdfFile() {
//        binding.addPdfFab.setOnClickListener {
//            startActivity(Intent(this, UploadPdfActivity::class.java))
//        }
//
//        binding.addCategoryBtn.setOnClickListener {
//            startActivity(Intent(this, CategoryAddActivity::class.java))
//        }
        //handle click, logout
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        /*Btnsheet menu*/
//        binding.btnNavView.setOnClickListener {
//            val view : View = layoutInflater.inflate(R.layout.bottomsheet_fragment, null)
//            val dialog = BottomSheetDialog(this)
//            dialog.setContentView(view)
//            dialog.show()
//        }

//        binding.btnNavView.setOnClickListener {
//            showBottomDialog()
//        }

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

    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottomsheet_fragment)

        val cancelButton: ImageView = dialog.findViewById(R.id.cancelButton)


        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
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
            //logged in, get and show user info
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


//
//
//        }
//
//        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
//        drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()
//
//        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
//            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
//                // Вызывается при изменении состояния панели (при открытии или закрытии)
//                // slideOffset - это значение от 0 до 1, где 0 - закрыта, 1 - полностью открыта
//                isDrawerOpen = slideOffset > 0.5 // Задайте желаемый порог открытия панели
//                // Обновляем состояние кнопок при изменении панели
//                updateButtonClickableState()
//            }
//
//            override fun onDrawerOpened(drawerView: View) {
//                // Вызывается, когда панель открывается
//            }
//
//            override fun onDrawerClosed(drawerView: View) {
//                // Вызывается, когда панель закрывается
//            }
//
//            override fun onDrawerStateChanged(newState: Int) {
//                // Вызывается при изменении состояния панели
//            }
//        })
//
//    }
//
//    override fun onPostCreate(savedInstanceState: Bundle?) {
//        super.onPostCreate(savedInstanceState)
//        // Вызывается после завершения создания активити
//
//        // Изначально устанавливаем состояние кнопок в зависимости от значения isDrawerOpen
//        updateButtonClickableState()
//    }
//
//    private fun updateButtonClickableState() {
//        // Обновляем состояние кнопок в зависимости от значения isDrawerOpen
//        if (isDrawerOpen) {
//            binding.logoutBtn.isClickable = false
//            binding.searchEt.isClickable = false
//            binding.categoriesRv.isClickable = false
//            binding.addCategoryBtn.isClickable = false
//            row_holder.deleteBtn.isClickable = false
//            row_holder.categoryTv.isClickable = false
//            binding.addPdfFab.isClickable = false
//
//        } else {
//            binding.logoutBtn.isClickable = true
//            binding.searchEt.isClickable = true
//            binding.categoriesRv.isClickable = true
//            binding.addCategoryBtn.isClickable = true
//            row_holder.deleteBtn.isClickable = true
//            row_holder.categoryTv.isClickable = true
//            binding.addPdfFab.isClickable = true
//        }
//    }
