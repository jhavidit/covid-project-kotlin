package com.dsckiet.covidtracker.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PendingPatient(
    val name:String="",
    val caseId:String="",
    val age:Int=0,
    val gender:String="",
    val phone:String="",
    val address:String="",
    val labName:String=""
) :Parcelable
