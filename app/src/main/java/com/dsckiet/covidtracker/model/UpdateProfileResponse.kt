package com.dsckiet.covidtracker.model


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UpdateProfileResponse(
    val data: Data,
    val error: Boolean,
    val message: String
) : Parcelable

@Parcelize
data class Data(
    val about: String,
    val address: String,
    val age: String,
    val contact: String,
    val empId: String,
    val hospital: String,
    val name: String
) : Parcelable