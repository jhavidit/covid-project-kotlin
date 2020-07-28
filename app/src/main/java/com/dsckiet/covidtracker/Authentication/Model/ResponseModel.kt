package com.dsckiet.covidtracker.Authentication.Model

data class ResponseModel(

    val message: String,
    val error: Boolean,
    val data: Data
)