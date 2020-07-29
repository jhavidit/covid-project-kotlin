package com.dsckiet.covidtracker.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ResponseModel (
    val message: String,
    val error: Boolean,
    val data: List<PatientDetails>?
) : Parcelable

/*
    Merge Pending Patient into Patient Details if possible
*/
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

@Parcelize
data class PatientDetails (
    @Json(name = "phone") val phoneNo: Long,
    val district: String,
    val address: String,
    val name: String,
    val age: Int,
    val gender: String,
    val lab: PatientLab,
    @Json(name = "_id") val patientId: String
) : Parcelable

@Parcelize
data class AssignPatientLevel (
    val level: String,
    val comments: String,
    val isDeclined: Boolean
) : Parcelable

@Parcelize
data class PatientLab (
    val name: String
) : Parcelable
