package com.dsckiet.covidtracker.profile.models

data class NewPasswordRequest(
    val oldPassword: String,
    val newPassword: String
)