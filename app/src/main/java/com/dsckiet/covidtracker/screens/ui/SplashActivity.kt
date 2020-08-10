package com.dsckiet.covidtracker.screens.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.authentication.TokenManager
import com.dsckiet.covidtracker.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    lateinit var tokenManager: TokenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_splash
        )
        tokenManager = TokenManager(this)
        val token = tokenManager.getAuthToken()
        if (token.isNullOrBlank()) {
            Handler().postDelayed({
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, 1000)
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}