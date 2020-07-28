package com.dsckiet.covidtracker

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.Authentication.LoginAPI
import com.dsckiet.covidtracker.Authentication.Model.RequestModel
import com.dsckiet.covidtracker.Authentication.Model.ResponseModel
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                //Login Request
                val cb = object : Callback<ResponseModel> {
                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        val loginResponse = response.body()

                        if (loginResponse?.message == "success" && !loginResponse.error) {

                            val h = response.headers().get("x-auth-token")
                            tokenManager.saveAuthToken(h!!)
                            startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                        }
                    }

                    @SuppressLint("LogNotTimber")
                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                        Log.i("Request","Failure")
                    }
                }
                LoginAPI.retrofitService.sendUserData(user).enqueue(cb)
            }
        } else {

            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }
}
