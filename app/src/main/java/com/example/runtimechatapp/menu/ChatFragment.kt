package com.example.runtimechatapp.menu

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtimechatapp.Adapter.ChatAdapter
import com.example.runtimechatapp.R
import com.example.runtimechatapp.data.ChatItem
import com.example.runtimechatapp.data.ItemMessage
import com.example.runtimechatapp.data.Message
import com.example.runtimechatapp.databinding.FragmentChatBinding
import com.example.runtimechatapp.ui.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Date
import java.util.Locale

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding

    private val user = FirebaseAuth.getInstance()
    private val dbFirebase = FirebaseDatabase.getInstance()
    private val chatList = mutableListOf<ChatItem>()
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.rvChat?.layoutManager = LinearLayoutManager(context)
        adapter = ChatAdapter(chatList) { chatItem ->

            Log.d("USER ID", "onViewCreated: ${chatItem.sentTo}")
            val intent = Intent(activity, MessageActivity::class.java).apply {
                putExtra("CHAT_ID", chatItem.sentTo)
                putExtra("USER_ID", chatItem.sentTo)
                putExtra("USER_NAME", chatItem.nameReceiver)
                putExtra("USER_PROFILE_PICTURE", chatItem.photoUrl)
            }
            startActivity(intent)
        }
        binding?.rvChat?.adapter = adapter
//        fetchChatRooms()
    }

    override fun onPause() {
        super.onPause()
        _binding = null
        chatList.clear()
    }

    override fun onResume() {
        super.onResume()
        fetchChatRooms()
    }


    private fun fetchChatRooms() {

        val userId = user.currentUser?.uid.toString()

        Log.d("USER LOGIN", "fetchChatRooms: $userId")

        dbFirebase.reference.child("chats").child(userId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val chatItems = mutableListOf<ChatItem>()

                for (chatSnapshot in snapshot.children) {
                    val chatMap = chatSnapshot.value as? Map<String, Any>
                    chatMap?.let { data ->
                        val sentTo = data["sentTo"] as? String
                        val lastMessage = data["lastMessage"] as? String
                        val timestamp = data["timestamp"] as? String
                        val nameReceiver = data["nameReceiver"] as? String
                        val photoUrl = data["photoUrl"] as? String
                        val itemList = (data["item"] as? List<Map<String, Any>>)?.map { itemMap ->
                            ItemMessage(
                                text = itemMap["text"] as? String,
                                timestamp = itemMap["timestamp"] as? String
                            )
                        } ?: emptyList()

                        val chatItem = ChatItem(
                            sentTo = sentTo,
                            lastMessage = lastMessage,
                            timestamp = timestamp,
                            nameReceiver = nameReceiver,
                            photoUrl = photoUrl
                        )
                        chatList.add(chatItem)
                    }
                }
                adapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error retrieve chat", Toast.LENGTH_SHORT).show()
            }

        })

//        dbFirebase.reference.child(userId)
//            .get()
//            .addOnSuccessListener { documents ->
//                chatList.clear()
//                if (documents != null) {
//                    val document = documents.getValue(Message::class.java)
//                    val chatId = document?.chatId.toString()
//                    val lastMessage = document?.lastMessage.toString()
//                    val userId1 = document?.sentBy.toString()
//                    val userId2 = document?.sentTo.toString()
//                    val username = document?.name.toString()
//                    val userProfilePicture = document?.photoUrl.toString()
//
//                    val chatItem = ChatItem(
//                        chatId,
//                        lastMessage,
//                        userId1,
//                        userId2,
//                        username,
//                        userProfilePicture
//                    )
//
//                    chatList.add(chatItem)
//
//
//                }



//        dbFirestore.collection("chats")
//            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener { documents ->
//                chatList.clear()
//                for (document in documents) {
//                    val chatId = document.id
//                    val lastMessage = document.getString("lastMessage") ?: ""
//                    val lastMessageTimestamp = document.getString("lastMessageTimestamp") ?: ""
//                    val userId1 = document.getString("userId1") ?: ""
//                    val userId2 = document.getString("userId2") ?: ""
//                    val username = document.getString("username") ?: ""
//                    val userProfilePicture = document.getString("userProfilePicture") ?: ""
//
//                    val chatItem = ChatItem(
//                        chatId,
//                        lastMessage,
//                        lastMessageTimestamp,
//                        userId1,
//                        userId2,
//                        username,
//                        userProfilePicture
//                    )
//
//                    chatList.add(chatItem)
//                }
//                adapter.notifyDataSetChanged()
//            }
//            .addOnFailureListener { exception ->
//                Log.e("ChatFragment", "Error fetching chat rooms: ", exception)
//            }
    }


}
