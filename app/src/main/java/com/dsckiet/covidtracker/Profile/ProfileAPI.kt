package com.dsckiet.covidtracker.Profile


import com.dsckiet.covidtracker.Password.PasswordResponse
import com.dsckiet.covidtracker.Profile.Models.NewPasswordRequest
import com.dsckiet.covidtracker.Profile.Models.ProfileResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*


interface profileAPIService {


    @GET("api/v1/users/profile")
    fun getProfile(@Header("x-auth-token") token: String): Call<ProfileResponse>

    @Headers("Content-Type:application/json")
    @POST("api/v1/users/change-pwd")
    fun changePassword(@Header("x-auth-token") token: String,@Body newpassword : NewPasswordRequest): Call<PasswordResponse>

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

object ProfileAPI {
    val retrofitService: profileAPIService by lazy {
        retrofitBuilder.create(profileAPIService::class.java)
    }
}