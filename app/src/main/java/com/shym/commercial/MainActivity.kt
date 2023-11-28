package com.shym.commercial


import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.shym.commercial.databinding.ActivityMainBinding
import com.shym.commercial.authorization.login.LoginActivity
import com.shym.commercial.authorization.register.RegisterActivity
import com.shym.commercial.extensions.setSafeOnClickListener

class MainActivity : AppCompatActivity() {

    private lateinit var switchMode: SwitchCompat
    private var nightMode: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
//        //switchMode
//        switchMode = binding.switchMode
//        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE)
//        nightMode = sharedPreferences.getBoolean("nightMode", false)
//        if (nightMode){
//            switchMode.isChecked = true
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        }
//        switchMode.setOnClickListener {
//            if (nightMode) {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                editor = sharedPreferences.edit()
//                editor.putBoolean("nightMode", false)
//            } else {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                editor = sharedPreferences.edit()
//                editor.putBoolean("nightMode", true)
//            }
//            editor.apply()
//        }


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
        val progressDialog = ProgressDialogUtil.showProgressDialog(this)

        ProgressDialogUtil.hideProgressDialog(progressDialog, destination, this)
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
