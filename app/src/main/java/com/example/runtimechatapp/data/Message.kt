package com.example.runtimechatapp.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message(
    val sentTo: String? = null,
    val lastMessage: String? = null,
//    val timestamp: String? = null,
    val nameReceiver: String? = null,
    val photoUrl: String? = null,
    val item: List<ItemMessage>? = listOf(),
    val senderId: String = "",
    val message: String = "",
    val timestamp: String = ""
)

data class ItemMessage(
    val text: String? =  null,
    val timestamp: String? = null
)
