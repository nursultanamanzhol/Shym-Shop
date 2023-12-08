package com.shym.commercial.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityRegisterBinding
import com.shym.commercial.extensions.setSafeOnClickListener
import com.shym.commercial.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        binding.registerBtn.setSafeOnClickListener {
            validateDataAndRegister()
        }
        binding.logInBtn.setSafeOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        registerViewModel.registrationResult.observe(this) { result ->
            result.onSuccess {
                val intent = Intent(this@RegisterActivity, MainUserPage::class.java)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this@RegisterActivity,
                    R.anim.slide_in_right,  //  slide_in_right
                    R.anim.slide_out_left   //  slide_out_left
                )
                startActivity(intent, options.toBundle())
                finish()
            }

            result.onFailure { e ->
                Toast.makeText(this, "Не удалось создать аккаунт: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        animation()
    }

    private fun validateDataAndRegister() {
        val name = binding.nameEt.text.toString().trim()
        val iinMain = binding.iinEt.text.toString().trim()
        val email = binding.emailEt.text.toString().trim()
        val quickAccessCode = binding.quickAccessCodeEt.text.toString().trim()
        val password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        registerViewModel.registerUser(name, iinMain, email, password, cPassword, quickAccessCode)
    }

    private fun animation() {
        val fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val bottom_down = AnimationUtils.loadAnimation(this, R.anim.bottom_down)

        // Настройка анимации
        binding.iconIv.animation = bottom_down

        val handler = Handler()
        val runnable = Runnable {
            binding.mainCnst.animation = fade_in
            binding.topCardView.animation = fade_in
            binding.logInBtn.animation = fade_in
            binding.registerBtn.animation = fade_in
            binding.card1.animation = fade_in
        }

        handler.postDelayed(runnable, 1000)
    }
}
