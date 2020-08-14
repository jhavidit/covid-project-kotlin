package com.dsckiet.covidtracker.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HospitalList(
    val message: String,
    val error: Boolean,
    val data: List<HospitalData>
) : Parcelable

@Parcelize
data class HospitalData(
    @Json(name = "_id") val hospitalId: String,
    val name: String = "",
    val address: String = "",
    val category:String = "",
    val contact:String= "",
    val availableBeds:Int=0
) : Parcelable

@Parcelize
data class AvailableHospital(
    val hospitalName: String = "",
    val hospitalAddress: String = "",
    val hospitalId: String = ""
) : Parcelable

