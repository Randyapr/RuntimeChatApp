//package com.example.runtimechatapp.ui
//
//import com.example.runtimechatapp.data.LocalData
//import com.example.runtimechatapp.data.Message
//import com.google.firebase.firestore.FirebaseFirestore
//
//class MessageSender(
//    private val firestore: FirebaseFirestore,
//    private val localDatabase: LocalData
//) {
//
//    fun sendMessage(fromUserId: String, toUserId: String, message: Message, onComplete: (Boolean) -> Unit) {
//        val documentIdLocal = localDatabase.getDocumentId(fromUserId, toUserId)
//        val chatDocumentId = documentIdLocal ?: "${fromUserId}_${toUserId}"
//
//        firestore.collection("Messages").document(chatDocumentId).get().addOnSuccessListener { doc ->
//            when {
//                doc.exists() -> {
//                    saveMessage(chatDocumentId, message, onComplete)
//                }
//                else -> {
//                    val reversedDocumentId = "${toUserId}_$fromUserId"
//                    firestore.collection("Messages").document(reversedDocumentId).get().addOnSuccessListener { reversedDoc ->
//                        when {
//                            reversedDoc.exists() -> {
//                                saveMessage(reversedDocumentId, message, onComplete)
//                            }
//                            else -> {
//                                // Handle case when there is no chat history
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun saveMessage(chatDocumentId: String, message: Message, onComplete: (Boolean) -> Unit) {
//        firestore.collection("Messages").document(chatDocumentId).collection("Chats").document(message.id).set(message)
//            .addOnSuccessListener {
//                onComplete(true)
//            }
//            .addOnFailureListener {
//                onComplete(false)
//            }
//    }
//}
