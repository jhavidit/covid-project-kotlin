package com.dsckiet.covidtracker.screens.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.authentication.LoginAPI
import com.dsckiet.covidtracker.authentication.TokenManager
import com.dsckiet.covidtracker.authentication.model.RequestModel
import com.dsckiet.covidtracker.authentication.model.ResponseModel
import com.dsckiet.covidtracker.databinding.ActivityLoginBinding
import com.dsckiet.covidtracker.utils.logs
import com.dsckiet.covidtracker.utils.toasts
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_login
        )

        tokenManager = TokenManager(this)
        val token = tokenManager.getAuthToken()

        /* To hide the warnings when user tried to enter details in text-fields again */
        binding.usernameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.isNotEmpty()) {
                    hideWarningViews()
                }
            }
        })
        binding.passwordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.isNotEmpty()) {
                    hideWarningViews()
                }
            }
        })

        if (token.isNullOrBlank()) {

            binding.authButton.setOnClickListener {

                hideKeyboard(it)
                if (binding.usernameInput.text!!.isEmpty()) {
                    binding.invalidEmailId.visibility = VISIBLE
                    binding.invalidEmailId.setText(R.string.email_can_not_be_empty)
                    binding.usernameInputLayout.boxStrokeColor =
                        ContextCompat.getColor(applicationContext, R.color.warning_red_color)
                } else if (binding.passwordInput.text!!.length < 6) {
                    binding.invalidPassword.visibility = VISIBLE
                    binding.invalidPassword.setText(R.string.invalid_password)
                    binding.passwordInputLayout.boxStrokeColor =
                        ContextCompat.getColor(applicationContext, R.color.warning_red_color)
                } else if (!binding.usernameInput.text!!.contains(
                        "@",
                        true
                    ) || !binding.usernameInput.text!!.contains(".", true)
                ) {
                    binding.invalidEmailId.visibility = VISIBLE
                    binding.invalidEmailId.setText(R.string.invalid_email_id)
                    binding.usernameInputLayout.boxStrokeColor =
                        ContextCompat.getColor(applicationContext, R.color.warning_red_color)
                } else {

                    binding.authButton.visibility = GONE
                    binding.authButtonAnim.visibility = VISIBLE
                    val email = binding.usernameInput.text.toString().trim()
                    val password = binding.passwordInput.text.toString().trim()
                    val user = RequestModel(
                        email,
                        password
                    )
                    //Login Request
                    val loginRequest = object : Callback<ResponseModel> {
                        override fun onResponse(
                            call: Call<ResponseModel>,
                            response: Response<ResponseModel>
                        ) {
                            val loginResponse = response.body()

                            if (loginResponse?.message == "success" && !loginResponse.error) {

                                val h = response.headers()["x-auth-token"]
                                tokenManager.saveAuthToken(h!!)
                                binding.authButtonAnim.visibility = GONE
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            } else {
                                binding.authButtonAnim.visibility = GONE
                                binding.authButton.visibility = VISIBLE
                                binding.invalidPassword.visibility = VISIBLE
                                binding.invalidPassword.setText(R.string.invalid_email_id_or_password)
                            }
                        }

                        override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                            binding.authButtonAnim.visibility = GONE
                            binding.authButton.visibility = VISIBLE
                            toasts(this@LoginActivity, "Network Problem")
                            logs("Request Failure: ${t.message}")
                        }
                    }
                    LoginAPI.retrofitService.sendUserData(user).enqueue(loginRequest)
                }
            }
        } else {

            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    private fun hideWarningViews() {
        binding.invalidPassword.visibility = GONE
        binding.invalidEmailId.visibility = GONE
        binding.usernameInputLayout.boxStrokeColor =
            ContextCompat.getColor(applicationContext, R.color.light_grey)
        binding.passwordInputLayout.boxStrokeColor =
            ContextCompat.getColor(applicationContext, R.color.light_grey)
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
