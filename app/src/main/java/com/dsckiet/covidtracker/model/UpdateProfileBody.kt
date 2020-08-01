package com.dsckiet.covidtracker.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import okhttp3.MultipartBody

@Parcelize
data class UpdateProfileBody(
    val about: String,
    val address: String,
    val age: String,
    val contact: String,
    val empId: String,
    val hospital: String,
    val name: String
): Parcelable