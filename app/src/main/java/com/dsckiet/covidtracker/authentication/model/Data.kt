package com.dsckiet.covidtracker.authentication.model

data class Data(
    val role: String,
    val _id: String,
    val name: String,
    val email: String,
    val password: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int

)