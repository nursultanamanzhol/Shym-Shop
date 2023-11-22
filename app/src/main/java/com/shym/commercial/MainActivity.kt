package com.shym.commercial

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.shym.commercial.databinding.ActivityMainBinding
import com.shym.commercial.authorization.LoginActivity
import com.shym.commercial.authorization.RegisterActivity


class MainActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        //handle click, login
        binding.loginBtn.setOnClickListener {
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

        //hand click, skip and continue to main screen
        binding.registerBtn.setOnClickListener {
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Загрузка...")

            // Показываем ProgressDialog
            progressDialog.show()

            // Создаем задачу для Handler, чтобы закрыть ProgressDialog через 2 секунды
            Handler().postDelayed({
                progressDialog.dismiss()

                // После закрытия ProgressDialog запускаем новую активность
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }, 2000) // 2000 миллисекунд (2 секунды)
        }

        //now lets connect with firebase

    }
}