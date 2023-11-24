package com.shym.commercial

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityOptionsCompat
import com.shym.commercial.databinding.ActivityMainBinding
import com.shym.commercial.authorization.login.LoginActivity
import com.shym.commercial.authorization.register.RegisterActivity
import com.shym.commercial.extensions.setSafeOnClickListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        animation()
        setupFullScreen()

        binding.loginBtn.setSafeOnClickListener { navigateTo(LoginActivity::class.java) }
        binding.registerBtn.setSafeOnClickListener { navigateTo(RegisterActivity::class.java) }

    }

    private fun setupFullScreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun navigateTo(destination: Class<*>) {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Загрузка...")
            show()
        }

        Handler().postDelayed({
            progressDialog.dismiss()
            val intent = Intent(this, destination)
            val options = ActivityOptionsCompat.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            startActivity(intent, options.toBundle())
            finish()
        }, 2000)
    }

    private fun animation() {
        //initialized animation
        var fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        var bottom_down = AnimationUtils.loadAnimation(this, R.anim.bottom_down)
        //settings the bottom down
        binding.iconIv.animation = bottom_down

        //handler
        var handler = Handler()
        var runnable = Runnable() {
            //lets set fadeIN animation on other layouts
            binding.loginBtn.animation = fade_in
            binding.registerBtn.animation = fade_in

        }

        handler.postDelayed(runnable, 1000)

    }


}
