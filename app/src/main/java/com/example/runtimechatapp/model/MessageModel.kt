package com.example.runtimechatapp.model

data class MessageModel(
    var messageId: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val message: String? = null,
    val timestamp: String? = null,
    var imageUrl: String? = null,
    var status: String? = "sent"
)