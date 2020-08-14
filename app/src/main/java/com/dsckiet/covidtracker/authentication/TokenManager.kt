package com.dsckiet.covidtracker.authentication

import android.content.Context
import android.content.SharedPreferences
import com.dsckiet.covidtracker.R

class TokenManager(context: Context) {

    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun deleteAuthToken() {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, null)
        editor.apply()
    }
}