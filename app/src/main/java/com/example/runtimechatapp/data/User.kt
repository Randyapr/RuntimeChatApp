package com.example.runtimechatapp.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties

data class User(
    val userId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val status: String = "Busy",
    val bio: String ="",
    val profile_picture: String = ""
)

