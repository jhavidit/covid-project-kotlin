package com.dsckiet.covidtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.authButton.setOnClickListener{
            when {
                binding.usernameInput.text!!.isEmpty() -> binding.usernameInput.error="Email is empty"
                binding.passwordInput.text!!.isEmpty() -> binding.passwordInput.error="Password is Empty"
                else -> {
                    val intent= Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}