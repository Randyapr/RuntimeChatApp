package com.example.runtimechatapp.model

data class MessageModel(
    val senderId: String? = null,
    val receiverId: String? = null,
    val message: String? = null,
    val timestamp: String? = null
)