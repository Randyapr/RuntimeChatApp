package com.example.runtimechatapp.data

class LocalData {
    private val chatMap = mutableMapOf<String, String>()

    fun getDocumentId(fromUserId: String, toUserId: String): String? {
        return chatMap["${fromUserId}_${toUserId}"] ?: chatMap["${toUserId}_${fromUserId}"]
    }


    fun saveDocumentId(fromUserId: String, toUserId: String, documentId: String) {
        chatMap["${fromUserId}_${toUserId}"] = documentId
    }
}
