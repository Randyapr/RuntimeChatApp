package com.example.runtimechatapp.data


data class Chat(
    val member: List<String> = emptyList(),
    val lastMessageSent: String? = null,
)

data class ChatMessage(
    val sentBy: String = "",
    val sentTo: String = "",
    val messageTime: Long = 0,
    val message: String = ""
)

data class UserChats(
    val chats: Map<String,String> = emptyMap()
)