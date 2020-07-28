package com.dsckiet.covidtracker.Authentication

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.dsckiet.covidtracker.Authentication.Model.ResponseModel
import com.dsckiet.covidtracker.Authentication.Model.RequestModel
import com.dsckiet.covidtracker.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object LoginRepository {

    private lateinit var tokenManager: TokenManager
    fun loginUser(model: RequestModel, context: Context) : Boolean{
        var isLoginSuccessful = false
        val callback = object : Callback<ResponseModel> {
            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {

                Log.i("Failed", t.message.toString())
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                isLoginSuccessful = false
            }

            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {

                Log.i("Done", response.code().toString())
                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()

                val loginResponse = response.body()

                if (loginResponse?.message == "success" && !loginResponse.error) {

                    Toast.makeText(context, "User Logined", Toast.LENGTH_SHORT).show()
                    val h = response.headers().get("x-auth-token")
                    tokenManager= TokenManager(context)
                    tokenManager.saveAuthToken(h!!)
                    isLoginSuccessful = true
                }
            }
        }
        LoginAPI.retrofitService.sendUserData(model).enqueue(callback)
        return isLoginSuccessful
    }
}