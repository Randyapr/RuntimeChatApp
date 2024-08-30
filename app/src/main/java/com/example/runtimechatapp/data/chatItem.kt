package com.example.runtimechatapp.data

data class ChatItem(
    val sentTo: String? = null,
    val lastMessage: String? = null,
    val timestamp: String? = null,
    val nameReceiver: String? = null,
    val photoUrl: String? = null,
    val item: List<ItemMessage>? = emptyList()

//    val chatId: String? = "",
//    val lastMessage: String? = "",
//    val userId1: String? = "",
//    val userId2: String? = "",
//    val username: String? = "",
//    val timestamp: String? = "",
//    val userProfilePicture: String? = ""
)

data class ChatUserItem(
    val userId: String? = "",
    val chatId: List<UserId>? = emptyList()
)

data class UserId(
    val id: List<String>? = emptyList()
)

