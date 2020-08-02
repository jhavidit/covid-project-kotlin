package com.dsckiet.covidtracker.Profile.Models


data class ProfileResponse (
    val message: String="",
    val error: Boolean,
    val data: Data?
)