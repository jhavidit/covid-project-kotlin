package com.dsckiet.covidtracker.Profile.Models

data class NewPasswordRequest(
    val oldPassword :String,
    val newPassword :String
)