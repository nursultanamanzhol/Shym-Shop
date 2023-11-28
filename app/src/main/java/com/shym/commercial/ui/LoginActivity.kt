package com.shym.commercial.ui
// LoginActivity.kt
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.shym.commercial.extensions.ProgressDialogUtil
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityLoginBinding
import com.shym.commercial.extensions.setSafeOnClickListener
import com.shym.commercial.repositories.AuthRepository
import com.shym.commercial.viewModel.AuthViewModel
import com.shym.commercial.viewModel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory(AuthRepository()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupFullScreen()
        animation()
        navPages()



        viewModel.loginSuccess.observe(this, Observer { success ->
            if (success) {
                startActivity(Intent(this, PasswordPage::class.java))
                finish()
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format...", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show()
        } else {
            loginUser()
        }
    }

    private fun loginUser() {
        viewModel.loginUser(email, password)
    }

    private fun setupFullScreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun navPages() {
        binding.loginBtn.setSafeOnClickListener { validateData() }
        binding.noAccountBtn.setSafeOnClickListener { navigateTo(RegisterActivity::class.java) }
    }


    private fun navigateTo(destination: Class<*>) {
        val progressDialog = ProgressDialogUtil.showProgressDialog(this)

        ProgressDialogUtil.hideProgressDialog(progressDialog, destination, this)
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
            binding.cardViewFaceBook.animation = fade_in
            binding.cardViewGoogle.animation = fade_in
            binding.cardViewTwitter.animation = fade_in
            binding.noAccountBtn.animation = fade_in
            binding.loginBtn.animation = fade_in

        }

        handler.postDelayed(runnable, 1000)

    }
}
