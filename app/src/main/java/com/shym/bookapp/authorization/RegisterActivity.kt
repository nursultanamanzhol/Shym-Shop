package com.shym.bookapp.authorization

import android.view.View
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import com.shym.bookapp.dashboard.DashboardUserActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.shym.bookapp.R
import com.shym.bookapp.databinding.ActivityRegisterBinding

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

        //handle back button click, goto previous screen
//        binding.backBtn.setOnClickListener {
//            onBackPressed() //goto previous screen
//        }

        binding.LoginTv.setOnClickListener {
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Загрузка...")

            // Показываем ProgressDialog
            progressDialog.show()

            // Создаем задачу для Handler, чтобы закрыть ProgressDialog через 2 секунды
            Handler().postDelayed({
                progressDialog.dismiss()

                // После закрытия ProgressDialog запускаем новую активность
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 2000) // 2000 миллисекунд (2 секунды)
        }

        //handle click, begin register
        binding.registerBtn.setOnClickListener {
            /*Steps
            * 1) Input Data
            * 2) Validate Data
            * 3) Create Account - Firebase Auth
            * 4) Save User Info - Firebase Realtime Database*/
            validateData()
        }

    }

    private var name = ""
    private var iinMain = ""
    private var email = ""
    private var password = ""


    private fun validateData() {
        //1) Input Data
        name = binding.nameEt.text.toString().trim()
        iinMain = binding.iinEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        //2) Validate Data
        if (name.isEmpty()) {
            //empty name...
            Toast.makeText(this, "Введите свое ФИО...", Toast.LENGTH_SHORT).show()
        } else if (iinMain.isEmpty()) {
            //empty iinMain...
            Toast.makeText(this, "Введите свои ИИН...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //invalid email pattern
            Toast.makeText(this, "Неверный шаблон электронной почты...", Toast.LENGTH_SHORT).show()

        } else if (password.isEmpty()) {
            //empty password
            Toast.makeText(this, "Введите пароль...", Toast.LENGTH_SHORT).show()
        } else if (cPassword.isEmpty()) {
            //empty password
            Toast.makeText(this, "Подтвердите пароль...", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            Toast.makeText(this, "Пароли не совпадает...", Toast.LENGTH_SHORT).show()
        } else {
            createUserAccount()
        }

    }

    private fun createUserAccount() {
        //3) Create Account - Firebase Auth

        //show progress
        progressDialog.setMessage("Создание учетной записи...")
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
                    "Не удалось создать учетную запись по причине ${e.message}...",
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
        hashMap["name"] = name
        hashMap["profileImage"] = "" //add empty, will do in profile edit
        hashMap["userType"] =
            "user" //possible values are user/admin, will change value to admin manually on firebase db
        hashMap["timestamp"] = timestamp

        //set data to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //user info saved, open user dashboard
                progressDialog.dismiss()
                Toast.makeText(this, "Account created...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                //failed adding data to db
                progressDialog.dismiss()
                Toast.makeText(this, "Failed saving user info  ${e.message}...", Toast.LENGTH_SHORT)
                    .show()

            }


    }
}