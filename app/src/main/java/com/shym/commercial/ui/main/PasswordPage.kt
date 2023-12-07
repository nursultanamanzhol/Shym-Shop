package com.shym.commercial.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.commercial.extensions.DialogUtils
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityPasswordPageBinding
import com.shym.commercial.extensions.setSafeOnClickListener

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
        loadUserInfo()

        val btn1: ConstraintLayout = binding.btn1
        val btn2: ConstraintLayout = binding.btn2
        val btn3: ConstraintLayout = binding.btn3
        val btn4: ConstraintLayout = binding.btn4
        val btn5: ConstraintLayout = binding.btn5
        val btn6: ConstraintLayout = binding.btn6
        val btn7: ConstraintLayout = binding.btn7
        val btn8: ConstraintLayout = binding.btn8
        val btn9: ConstraintLayout = binding.btn9
        val btn0: ConstraintLayout = binding.btn0
        val checkBtn: ConstraintLayout = binding.btnCheck
        val deleteBtn: ConstraintLayout = binding.deleteBtn
        val passwordText: TextView = binding.passwordTex

        btn1.setOnClickListener { appendDigit(1) }
        btn2.setOnClickListener { appendDigit(2) }
        btn3.setOnClickListener { appendDigit(3) }
        btn4.setOnClickListener { appendDigit(4) }
        btn5.setOnClickListener { appendDigit(5) }
        btn6.setOnClickListener { appendDigit(6) }
        btn7.setOnClickListener { appendDigit(7) }
        btn8.setOnClickListener { appendDigit(8) }
        btn9.setOnClickListener { appendDigit(9) }
        btn0.setOnClickListener { appendDigit(0) }

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
                    deleteBtn.setOnClickListener {
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

    private fun loadUserInfo() {
        //db firebase user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    //set date
                    binding.nameTv.text = name
                    //set image
                    try {
                        Glide.with(this@PasswordPage)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileImg)
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}