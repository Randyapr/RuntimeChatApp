package com.example.runtimechatapp.model

data class ChatListModel(
    val uid: String? = null,
    val name: String? = null,
    val profileImage: String? = null,
    val lastMessage: String? = null,
    val timestamp: Long = 0,
    val unreadCount: Int = 0,
    val lastMessageStatus: String? = null
)