package com.dsckiet.covidtracker.profile.models


data class ProfileResponse(
    val message: String = "",
    val error: Boolean,
    val data: Data?
)