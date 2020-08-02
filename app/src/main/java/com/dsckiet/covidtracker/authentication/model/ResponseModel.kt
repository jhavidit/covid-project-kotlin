package com.dsckiet.covidtracker.authentication.model

data class ResponseModel(

    val message: String,
    val error: Boolean,
    val data: Data
)