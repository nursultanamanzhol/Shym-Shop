package com.shym.commercial.category

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.shym.commercial.users_role.admin.DashboardAdminActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.shym.commercial.databinding.ActivityCategoryAddBinding

class CategoryAddActivity : AppCompatActivity() {

    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityCategoryAddBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Скрываем навигационную панель и часы
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

//firebase suth
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle
        binding.submitBtn.setOnClickListener {
            validateDate()
        }

    }

    private var category = ""
    private fun validateDate() {
        //data get
        category = binding.categoryEt.text.toString().trim()

        if (category.isEmpty()) {
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        } else {
            addCategoryFirebase()
        }

    }

    private fun addCategoryFirebase() {
        //progress
        progressDialog.show()
        val timestamp = System.currentTimeMillis()
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                //success for commit
                progressDialog.dismiss()
                Toast.makeText(this, "Added Successfully...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, DashboardAdminActivity::class.java))
                finish()

            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
                //failed
            }
    }
}