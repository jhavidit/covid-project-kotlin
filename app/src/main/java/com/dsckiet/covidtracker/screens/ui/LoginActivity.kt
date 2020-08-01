package com.dsckiet.covidtracker.screens.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.Authentication.LoginAPI
import com.dsckiet.covidtracker.Authentication.Model.RequestModel
import com.dsckiet.covidtracker.Authentication.Model.ResponseModel
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.R
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
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_login
        )

        tokenManager = TokenManager(this)
        val token = tokenManager.getAuthToken()

        if (token.isNullOrBlank()) {

            auth_button.setOnClickListener {
                binding.invalidPassword.visibility = GONE
                binding.invalidEmailId.visibility = GONE
                binding.usernameInputLayout.boxStrokeColor = Color.parseColor("#707070")
                binding.passwordInputLayout.boxStrokeColor = Color.parseColor("#707070")


                if (binding.usernameInput.text!!.isEmpty()) {
                    binding.invalidEmailId.visibility = VISIBLE
                    binding.invalidEmailId.text = "Email can not be empty"
                    binding.usernameInputLayout.boxStrokeColor = Color.RED
                } else if (binding.passwordInput.text!!.length < 6) {
                    binding.invalidPassword.visibility = VISIBLE
                    binding.invalidPassword.text = "Password too small"
                    binding.passwordInputLayout.boxStrokeColor = Color.RED
                } else if (!binding.usernameInput.text!!.contains(
                        "@",
                        true
                    ) || !binding.usernameInput.text!!.contains(".", true)
                ) {
                    binding.invalidEmailId.visibility = VISIBLE
                    binding.invalidEmailId.text = "Invalid Email ID"
                    binding.usernameInputLayout.boxStrokeColor = Color.RED
                } else {

                    auth_button.visibility = GONE
                    progress_login.visibility = VISIBLE
                    val email = username_input.text.toString().trim()
                    val password = password_input.text.toString().trim()
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
                                binding.progressLogin.visibility = View.GONE
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            } else {
                                binding.progressLogin.visibility = GONE
                                binding.invalidPassword.visibility = VISIBLE
                                binding.invalidPassword.text = "Invalid email id or Password"
                                binding.authButton.visibility = VISIBLE
                            }
                        }

                        @SuppressLint("LogNotTimber")
                        override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                            Log.i("Request", "Failure : ${t.message}")
                        }
                    }
                    LoginAPI.retrofitService.sendUserData(user).enqueue(cb)
                }
            }
        } else {

            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }
}
