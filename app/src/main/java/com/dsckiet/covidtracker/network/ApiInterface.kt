package com.dsckiet.covidtracker.network

import com.dsckiet.covidtracker.model.AssignPatient
import com.dsckiet.covidtracker.model.AssignPatientLevel
import com.dsckiet.covidtracker.model.ResponseModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://covid-project-gzb.herokuapp.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(OkHttpClient.Builder().build())
    .build()

interface ApiInterface {
    @GET("api/v1/doctors/patients/declined")
    fun getPatientsData(
        @Header("x-auth-token") token: String
    ): Call<ResponseModel>

    @GET("api/v1/doctors/patients/unassigned")
    fun getUnassignedPatientsData(
        @Header("x-auth-token") token: String
    ): Call<ResponseModel>

    @POST("api/v1/doctors/patients/attend/{patientId}")
    fun diagnosisBeginRequest(
        @Header("x-auth-token") token: String,
        @Path("patientId") patientId: String
    ): Call<ResponseModel>

    @POST("api/v1/doctors/patients/level/{patientId}")
    fun assignPatientLevel(
        @Header("x-auth-token") token: String, @Path("patientId") patientId: String,
        @Body patientLevel: AssignPatientLevel
    ): Call<AssignPatient>
}

object PatientsApi {
    val retrofitService: ApiInterface by lazy {
        retrofit.create(ApiInterface::class.java)
    }
}