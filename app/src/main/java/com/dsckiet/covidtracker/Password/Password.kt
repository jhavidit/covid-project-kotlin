package com.dsckiet.covidtracker.Password

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.Profile.Models.NewPasswordRequest
import com.dsckiet.covidtracker.Profile.ProfileAPI
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.databinding.ActivityPasswordBinding
import com.dsckiet.covidtracker.screens.ui.MainActivity
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Password : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var binding: ActivityPasswordBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_password
        )
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.doneButton.setOnClickListener {

            hideKeyboard(it)
            tokenManager = TokenManager(this)

            val token = tokenManager.getAuthToken()
            val oldPass = binding.oldPasswordInput.text.toString()
            val newPass = binding.newPasswordInput.text.toString()
            val confirmPass = binding.confirmPasswordInput.text.toString()

            if (token != null) {

                if (newPass == confirmPass && newPass.length >= 6) {

                    val changePass = NewPasswordRequest(oldPass, newPass)
                    ProfileAPI.retrofitService.changePassword(
                        token = token,
                        newpassword = changePass
                    )
                        .enqueue(object :
                            Callback<PasswordResponse> {
                            @SuppressLint("LogNotTimber")
                            override fun onFailure(call: Call<PasswordResponse>, t: Throwable) {
                                Log.i("msg", "ERROR-${t.message}")
                                Snackbar.make(
                                    binding.passwordLayout,
                                    "Some problem occurred check your network connection or restart the app",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }

                            override fun onResponse(
                                call: Call<PasswordResponse>,
                                response: Response<PasswordResponse>
                            ) {
                                val passRespone = response.body()
                                if (response.code() == 200 && passRespone!!.message == "success" && !passRespone.error) {
                                    Toast.makeText(
                                        this@Password,
                                        "Password Changed",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val i = Intent(this@Password, MainActivity::class.java)
                                    i.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(i)
                                }
                            }
                        })
                } else {

                    if (oldPass.length < 6) {
                        binding.errorOldPassword.visibility = View.VISIBLE
                        binding.errorOldPassword.text = "Wrong Password"
                    } else if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                        binding.errorOldPassword.visibility = View.GONE
                        if (oldPass.isEmpty()) {
                            binding.errorOldPassword.text = "Input missing"
                            binding.errorOldPassword.visibility = View.VISIBLE
                        }
                        if (newPass.isEmpty()) {
                            binding.errorNewPassword.text = "Input missing"
                            binding.errorNewPassword.visibility = View.VISIBLE
                        }
                        if (confirmPass.isEmpty()) {
                            binding.errorConfirmPassword.text = "Input missing"
                            binding.errorConfirmPassword.visibility = View.VISIBLE
                        }
                    } else if (newPass.length <= 5) {
                        binding.errorNewPassword.visibility = View.VISIBLE
                        binding.errorConfirmPassword.visibility = View.VISIBLE
                        binding.errorNewPassword.text = "Password too short"
                        binding.errorConfirmPassword.text = "Password too short"
                    } else if (newPass !== confirmPass) {

                        binding.errorNewPassword.visibility = View.VISIBLE
                        binding.errorConfirmPassword.visibility = View.VISIBLE
                        binding.errorNewPassword.text = "Password does not match"
                        binding.errorConfirmPassword.text = "Password does not match"
                    }
                    else{
                        Snackbar.make(
                            binding.passwordLayout,
                            "Could not change password",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                }
            }
        }

    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}