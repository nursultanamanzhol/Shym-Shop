package com.shym.bookapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.shym.bookapp.databinding.ActivityMainBinding
import com.shym.bookapp.authorization.LoginActivity
import com.shym.bookapp.authorization.RegisterActivity
import com.shym.bookapp.user.DashboardUserActivity


class MainActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


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