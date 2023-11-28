package com.shym.commercial.authorization.register

import android.app.ProgressDialog
import com.shym.commercial.extensions.setSafeOnClickListener
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import com.shym.commercial.users_role.customer.DashboardUserActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityRegisterBinding
import android.view.View
import android.view.animation.AnimationUtils
import com.shym.commercial.DialogUtils
import com.shym.commercial.ProgressDialogUtil
import com.shym.commercial.authorization.login.LoginActivity
import com.shym.commercial.users_role.MainUserPage

class RegisterActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityRegisterBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        animation()
        navPages()

        val iinEt = findViewById<EditText>(R.id.iinEt)
        val iinMain = findViewById<TextInputLayout>(R.id.iin_main)

        iinEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length ?: 0 < 12) {
                    iinMain.error = "ИИН должен содержать 12 цифр"
                    iinMain.isErrorEnabled = true
                } else {
                    iinMain.error = null
                    iinMain.isErrorEnabled = false
                }
            }
        })


        // Скрываем навигационную панель и часы
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // Если у вас есть ActionBar, скройте его
//        supportActionBar?.hide()


        // firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog, will show while creating account | Register user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        /*Steps
                    * 1) Input Data
                    * 2) Validate Data
                    * 3) Create Account - Firebase Auth
                    * 4) Save User Info - Firebase Realtime Database*/
        //handle click, begin register


    }

    private fun navPages() {
        binding.logInBtn.setSafeOnClickListener { navigateTo(LoginActivity::class.java) }
        binding.registerBtn.setSafeOnClickListener {
            validateData()
        }
    }

    private fun navigateTo(destination: Class<*>) {
        val progressDialog = ProgressDialogUtil.showProgressDialog(this)

        ProgressDialogUtil.hideProgressDialog(progressDialog, destination, this)
    }

    private var name = ""
    private var iinMain = ""
    private var email = ""
    private var password = ""
    private var quickAccessCode = ""


    private fun validateData() {
        //1) Input Data
        name = binding.nameEt.text.toString().trim()
        iinMain = binding.iinEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        quickAccessCode = binding.quickAccessCodeEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        //2) Validate Data
        if (name.isEmpty()) {
            //empty name...
            Toast.makeText(this, "Enter your full name...", Toast.LENGTH_SHORT).show()
        } else if (iinMain.isEmpty()) {
            //empty iinMain...
            Toast.makeText(this, "Enter your Individual ID number...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //invalid email pattern
            Toast.makeText(this, "Invalid e-mail template...", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            //empty password
            Toast.makeText(this, "Enter your password...", Toast.LENGTH_SHORT).show()
        } else if (cPassword.isEmpty()) {
            //empty password
            Toast.makeText(this, "Confirm password...", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            Toast.makeText(this, "Passwords don't match...", Toast.LENGTH_SHORT).show()
        } else if (quickAccessCode.isEmpty()) {
            //empty quickAccessCode
            Toast.makeText(this, "Think of a shortcut code...", Toast.LENGTH_SHORT).show()
        } else {
            createUserAccount()
        }

    }

    private fun createUserAccount() {
        //3) Create Account - Firebase Auth

        //show progress with custom message
        val progressDialog = DialogUtils.createProgressDialog(this, "Creating an account...")
        progressDialog.show()

        //create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //account created, now add user info in db
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                //failed creating account
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to create an account due to ${e.message}...",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun updateUserInfo() {
        // 4) Save User Info - Firebase Realtime Database

        progressDialog.setMessage("Saving user info...")

        //timestamp
        val timestamp = System.currentTimeMillis()

        //get current user uid, since user is registered so we can get it now
        val uid = firebaseAuth.uid

        //setup data to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["iiMain"] = iinMain
        hashMap["quickAccessCode"] = quickAccessCode
        hashMap["name"] = name
        hashMap["profileImage"] = "" //add empty, will do in profile edit
        hashMap["userType"] =
            "user"
        hashMap["categories"] =
            "company" //possible values are user/admin, will change value to admin manually on firebase db
        hashMap["timestamp"] = timestamp

        //set data to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //user info saved, open user dashboard
                progressDialog.dismiss()
                Toast.makeText(this, "Account created...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, MainUserPage::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                //failed adding data to db
                progressDialog.dismiss()
                Toast.makeText(this, "Failed saving user info  ${e.message}...", Toast.LENGTH_SHORT)
                    .show()

            }
    }

    private fun animation() {
        //initialized animation
        var fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        var bottom_down = AnimationUtils.loadAnimation(this, R.anim.bottom_down)
        //settings the bottom down
        binding.topConst.animation = bottom_down

        //handler
        var handler = Handler()
        var runnable = Runnable() {
            //lets set fadeIN animation on other layouts
            binding.topCardView.animation = fade_in
            binding.card1.animation = fade_in
            binding.logInBtn.animation = fade_in
            binding.registerBtn.animation = fade_in

        }

        handler.postDelayed(runnable, 1000)

    }
}