package com.dsckiet.covidtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.Authentication.LoginRepository
import com.dsckiet.covidtracker.Authentication.Model.RequestModel
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        tokenManager = TokenManager(this)
        val token = tokenManager.getAuthToken()

        if (token.isNullOrBlank()) {

            auth_button.setOnClickListener {

                val email = username_input.text.toString()
                val password = password_input.text.toString()
                val user = RequestModel(
                    email,
                    password
                )


                LoginRepository.loginUser(user, this)
                if(LoginRepository.loginUser(user,this)){
                    startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                }
                Log.i("Submit_btn", "Clicked")

            }
        } else {

            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }
}
