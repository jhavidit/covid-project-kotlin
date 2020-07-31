package com.dsckiet.covidtracker.Authentication

import com.dsckiet.covidtracker.Authentication.Model.ResponseModel
import com.dsckiet.covidtracker.Authentication.Model.RequestModel
import com.dsckiet.covidtracker.model.UpdateProfileBody
import com.dsckiet.covidtracker.model.UpdateProfileResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*


interface APIService {

    @Headers("Content-Type:application/json")
    @POST("api/v1/users/login/doctor")
    fun sendUserData(
        @Body userLogin: RequestModel
    ): Call<ResponseModel>

    @Headers("Content-Type:application/json")
    @PUT("api/v1/doctors/{id}")
    fun updateUserProfile(
        @Header("x-auth-token") token: String,
        @Path("id") id: String,
        @Body body: UpdateProfileBody

    ): Call<UpdateProfileResponse>


}

private const val BASE_URL = "https://covid-project-gzb.herokuapp.com/"

private val client = OkHttpClient.Builder().build()

val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofitBuilder = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(client)
    .build()

object LoginAPI {
    val retrofitService: APIService by lazy {
        retrofitBuilder.create(APIService::class.java)
    }
}