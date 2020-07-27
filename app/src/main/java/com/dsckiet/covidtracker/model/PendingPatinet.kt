package com.dsckiet.covidtracker.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PendingPatient(
val name:String,
val caseId:String,
val age:Int
) :Parcelable
