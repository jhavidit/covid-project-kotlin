package com.dsckiet.covidtracker.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity

@SuppressLint("LogNotTimber")
fun logs(msg: String) = Log.d("test->", " $msg")

fun toasts(ctx: Context, msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
